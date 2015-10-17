var BWFLA = BWFLA || {};

// Setup client-side handling of the embedded iframe
BWFLA.setupIFrameHandling = function(iframe)
{
	var getLocation = function(href) 
	{
	    var l = document.createElement("a");
	    l.href = href;
	    return l;
	};
	
	function onMessageReceived(message) 
	{
		var iFrameLocation = getLocation(iframe.src);
		if (message.origin !== iFrameLocation.origin)
			return;

		var event = null;
		try 
		{
			event = JSON.parse(message.data);
		}
		catch (error) 
		{
			console.warn("Invalid JSON message received!");
			return;
		}

		var opcode = event.opcode;
		if (opcode === "resize") 
		{
			iframe.width  = event.w;
			iframe.height = event.h;
		}
	}

	window.addEventListener("message", onMessageReceived, false);
};
