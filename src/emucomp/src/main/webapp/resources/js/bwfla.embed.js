
var BWFLA = BWFLA || {};

// Setup client-side handling of the embedded iframe
BWFLA.setupIFrameHandling = function(iframe)
{
	var parseUrl = function(href) 
	{
	    var l = document.createElement("a");
	    l.href = href;
	    return l;
	};
	
	// Define the message-handler
	function onMessageReceived(message) 
	{	
		var iframeLocation = parseUrl(iframe.src); 
		
		if (message.origin !== iframeLocation.origin)
			return;

		// Parse received message
		var event = null;
		try {
			event = JSON.parse(message.data);
		}
		catch (error) {
			console.warn("Invalid JSON message received!");
			return;
		}

		var opcode = event.opcode;
		
		if (opcode === "resize") {
			iframe.width  = event.w;
			iframe.height = event.h;
		}
	}

	window.addEventListener("message", onMessageReceived, false);
};
