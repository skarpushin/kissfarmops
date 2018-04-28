<%@ page pageEncoding="UTF-8"%>
<%@ page session="false"%>

<script type="text/javascript">
var sipl = setInterval(function() {
	if (/loaded|complete/.test(document.readyState)) {
		clearInterval(sipl);
		sipl = true;
		for (i=0; i<onDocumentReady.subscribers.length; i++) {
			onDocumentReady.subscribers[i]();
		}
	}
}, 10);

var onDocumentReady = function(handler) {
	if (sipl === true) return handler();
	var getType = {};
	if (!(handler && getType.toString.call(handler) === '[object Function]')) { return; }
	onDocumentReady.subscribers.push(handler);
};
onDocumentReady.subscribers = [];
</script>
