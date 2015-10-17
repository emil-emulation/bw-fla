function isPrimefacesCompatible() {
	if($.browser.safari){
			return (jQuery.versionNumber >= 5)
	} else if($.browser.opera){
			return (jQuery.versionNumber >= 11)
	} else if($.browser.msie){
			return ($.browser.versionNumber >= 8)
	} else if($.browser.mozilla){
			return ($.browser.versionNumber >= 4)
	} else if($.browser.chrome){
			return ($.browser.versionNumber >= 10)
	}
}

function read_cookie(k,r){
	return (r=RegExp('(^|; )' + encodeURIComponent(k) + '=([^;]*)').exec(document.cookie)) ? r[2] : null;
}