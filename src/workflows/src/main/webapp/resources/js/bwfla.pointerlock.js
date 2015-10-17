
var BWFLA = BWFLA || {};


/** Requests a pointer-lock on given element, if supported by the browser. */
BWFLA.requestPointerLock = function(target, event)
{
	function lockPointer() {
		var havePointerLock = 'pointerLockElement' in document
		                      || 'mozPointerLockElement' in document
		                      || 'webkitPointerLockElement' in document;
	
		if (!havePointerLock) {
			console.warn("Your browser does not support the PointerLock API!");
			console.warn("Using relative mouse is not possible.");
			return;
		}
	
		// Activate pointer-locking
		target.requestPointerLock = target.requestPointerLock
		                            || target.mozRequestPointerLock
		                            || target.webkitRequestPointerLock;

		target.requestPointerLock();
	};
	
	function enableLockEventListener()
	{
		target.addEventListener(event, lockPointer, false);
	};
	
	function disableLockEventListener()
	{
		target.removeEventListener(event, lockPointer, false);
	};
	
	function onPointerLockChange() {
		if (document.pointerLockElement === target
				|| document.mozPointerLockElement === target
				|| document.webkitPointerLockElement === target) {
			// Pointer was just locked
			console.debug("Pointer was locked!");
			target.isPointerLockEnabled = true;
			disableLockEventListener();
		} else {
			// Pointer was just unlocked
			console.debug("Pointer was unlocked.");
			target.isPointerLockEnabled = false;
			enableLockEventListener();
		}
	};

	// Hook for pointer lock state change events
	document.addEventListener('pointerlockchange', onPointerLockChange, false);
	document.addEventListener('mozpointerlockchange', onPointerLockChange, false);
	document.addEventListener('webkitpointerlockchange', onPointerLockChange, false);
	
	enableLockEventListener();
};


/** Hides the layer containing client-side mouse-cursor. */
BWFLA.hideClientCursor = function(display)
{
	var layer = display.getCursorLayer();
	var element = layer.getElement();
	$(element).hide();
};


/** Shows the layer containing client-side mouse-cursor. */
BWFLA.showClientCursor = function(display)
{
	var layer = display.getCursorLayer();
	var element = layer.getElement();
	$(element).show();
};
