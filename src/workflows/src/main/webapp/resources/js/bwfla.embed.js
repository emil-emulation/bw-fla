var BWFLA = BWFLA || {};

// Setup client-side handling of the embedded iframe
BWFLA.setupIFrameHandling = function(iframe)
{	
	function onMessageReceived(message) 
	{
		if (message.origin !== iframe.src.match(/^.+\:\/\/[^\/]+/)[0])
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
