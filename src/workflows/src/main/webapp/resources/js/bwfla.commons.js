function isPrimefacesCompatible() {
	var result = false;
	
	if ($.browser.safari) {
		result = ($.browser.versionNumber >= 5);
	} else if ($.browser.opera) {
		result = ($.browser.versionNumber >= 11);
	} else if ($.browser.msie) {
		result = ($.browser.versionNumber >= 10);
	} else if ($.browser.msedge) {
		result = true;
	} else if ($.browser.mozilla) {
		result = ($.browser.versionNumber >= 4);
	} else if ($.browser.chrome) {
		result = ($.browser.versionNumber >= 10);
	}
	
	return result;
}

function read_cookie(k,r){
	return (r=RegExp('(^|; )' + encodeURIComponent(k) + '=([^;]*)').exec(document.cookie)) ? r[2] : null;
}