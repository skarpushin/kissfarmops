package org.kissfarm.controller.services.nodes.impl;

import org.kissfarm.controller.security.SecurityConstantsEx;
import org.kissfarm.controller.services.nodes.api.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.ObjectUtils;
import org.summerb.approaches.jdbccrud.impl.wireTaps.EasyCrudWireTapNoOpImpl;
import org.summerb.approaches.security.api.exceptions.NotAuthorizedException;
import org.summerb.approaches.springmvc.security.SecurityConstants;
import org.summerb.approaches.validation.FieldValidationException;
import org.summerb.microservices.users.api.PasswordService;
import org.summerb.microservices.users.api.PermissionService;
import org.summerb.microservices.users.api.UserService;
import org.summerb.microservices.users.api.dto.User;
import org.summerb.microservices.users.api.exceptions.UserNotFoundException;

import com.google.common.base.Preconditions;

/**
 * This wiretap replicates all changes from Node row to User service
 * infrastructure to enable nodes to actually login to the system and security
 * subsystem to orchestrate all nodes actions
 * 
 * @author Sergey Karpushin
 *
 */
public class NodeToUserReplicationWireTap extends EasyCrudWireTapNoOpImpl<String, Node> {
	private Logger log = LoggerFactory.getLogger(getClass());

	@Autowired
	private UserService userService;
	@Autowired
	private PermissionService permissionService;
	@Autowired
	private PasswordService passwordService;

	@Override
	public boolean requiresFullDto() {
		return true;
	}

	@Override
	public boolean requiresOnCreate() throws FieldValidationException, NotAuthorizedException {
		return true;
	}

	@Override
	public void afterCreate(Node dto) throws FieldValidationException, NotAuthorizedException {
		User u = new User();
		u.setUuid(dto.getId());
		u.setDisplayName(dto.getHostName());
		u.setEmail(buildEmail(dto));
		u = userService.createUser(u);

		try {
			passwordService.setUserPassword(u.getUuid(), dto.getPassword());
		} catch (UserNotFoundException e) {
			throw new RuntimeException(
					"Potentially impossible case, we just registered user but passwordService tells user not found: \""
							+ u.getUuid() + "\"",
					e);
		}

		permissionService.grantPermission(SecurityConstants.DOMAIN, u.getUuid(), null, SecurityConstantsEx.ROLE_NODE);
	}

	private String buildEmail(Node dto) {
		// return dto.getPublicIp() + "@node.ip";
		return dto.getId() + "@node.id";
	}

	@Override
	public boolean requiresOnUpdate() throws NotAuthorizedException, FieldValidationException {
		return true;
	}

	@Override
	public void afterUpdate(Node from, Node to) throws NotAuthorizedException, FieldValidationException {
		try {
			Preconditions.checkState(from.getId().equals(to.getId()), "ID must not be changed");
			User u = userService.getUserByUuid(from.getId());
			if (!ObjectUtils.nullSafeEquals(to.getHostName(), u.getDisplayName())
					|| !ObjectUtils.nullSafeEquals(buildEmail(to), u.getEmail())
					|| !ObjectUtils.nullSafeEquals(to.isBlocked(), u.getIsBlocked())) {
				u.setDisplayName(to.getHostName());
				u.setEmail(buildEmail(to));
				u.setIsBlocked(to.isBlocked());
				userService.updateUser(u);
			}

			if (!passwordService.isUserPasswordValid(u.getUuid(), to.getPassword())) {
				passwordService.setUserPassword(u.getUuid(), to.getPassword());
			}
		} catch (UserNotFoundException e) {
			log.warn("Was trying to replicate user changes to node " + from.getId()
					+ " record, but user wasn't found. Will try to create one", e);
			afterCreate(to);
		}
	}

	@Override
	public boolean requiresOnDelete() throws FieldValidationException, NotAuthorizedException {
		return true;
	}

	@Override
	public void afterDelete(Node dto) throws FieldValidationException, NotAuthorizedException {
		permissionService.revokeUserPermissions(SecurityConstants.DOMAIN, dto.getId());

		// NOTE: apparently there is no way to explicitly remove user password

		try {
			userService.deleteUserByUuid(dto.getId());
		} catch (UserNotFoundException e) {
			// well, that's weird
			log.warn("Was trying to delete user related to node " + dto.getId() + ", but user wasn't found", e);
		}
	}
}
