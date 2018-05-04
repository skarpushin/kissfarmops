package org.kissfarm.agent.lifecycle;

import java.util.concurrent.Callable;

/**
 * Base interface for different lifecycle states of the agent.
 * 
 * {@link #call()} will return next state or null if application should exit
 * 
 * @author Sergey Karpushin
 *
 */
public interface Lifecycle extends Callable<Lifecycle> {

}
