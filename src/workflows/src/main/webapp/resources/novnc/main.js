"use strict";

var rfb;

function passwordRequired(rfb) {
    var msg;
    msg = '<form onsubmit="return setPassword();"';
    msg += '  style="margin-bottom: 0px">';
    msg += 'Password Required: ';
    msg += '<input type=password size=10 id="password_input" class="noVNC_status">';
    msg += '<\/form>';
    $D('noVNC_status_bar').setAttribute("class", "noVNC_status_warn");
    $D('noVNC_status').innerHTML = msg;
}
function setPassword() {
	rfb.sendPassword($D('password_input').value);
	return false;
}
function sendCtrlAltDel() {
	rfb.sendCtrlAltDel();
	return false;
}
function updateState(rfb, state, oldstate, msg) {
	var s, sb, cad, level;
	s = $D('noVNC_status');
	sb = $D('noVNC_status_bar');
	cad = $D('sendCtrlAltDelButton');
	switch (state) {
	case 'failed':
		level = "error";
		break;
	case 'fatal':
		level = "error";
		break;
	case 'normal':
		level = "normal";
		break;
	case 'disconnected':
		level = "normal";
		break;
	case 'loaded':
		level = "normal";
		break;
	default:
		level = "warn";
		break;
	}

	if (state === "normal") {
		cad.disabled = false;
	} else {
		cad.disabled = true;
	}

	if (typeof (msg) !== 'undefined') {
		sb.setAttribute("class", "noVNC_status_" + level);
		s.innerHTML = msg;
	}
}

function startNoVnc(proxyhost, proxyport, vnchost, vncport) {
	var password, path;
	
	$D('sendCtrlAltDelButton').style.display = "inline";
	$D('sendCtrlAltDelButton').onclick = sendCtrlAltDel;

	WebUtil.init_logging(WebUtil.getQueryVar('logging', 'warn'));
	document.title = unescape(WebUtil.getQueryVar('title', 'noVNC'));
	password = WebUtil.getQueryVar('password', '');

	if ((!proxyhost) || (!proxyport)) {
		updateState('failed', "Must specify proxy host and port in URL");
		return;
	}
	
	if ((!vnchost) || (!vncport)) {
		updateState('failed', "Must specify vnc host and port in URL");
		return;
	}
	
	path = '?token=' + vnchost + ":" + vncport;

	rfb = new RFB({
		'target' : $D('noVNC_canvas'),
		'encrypt' : WebUtil.getQueryVar('encrypt',
				(window.location.protocol === "https:")),
		'repeaterID' : WebUtil.getQueryVar('repeaterID', ''),
		'true_color' : WebUtil.getQueryVar('true_color', true),
		'local_cursor' : WebUtil.getQueryVar('cursor', true),
		'shared' : WebUtil.getQueryVar('shared', true),
		'view_only' : WebUtil.getQueryVar('view_only', false),
		'updateState' : updateState,
		'onPasswordRequired' : passwordRequired
	});
	rfb.connect(proxyhost, proxyport, password, path);
};