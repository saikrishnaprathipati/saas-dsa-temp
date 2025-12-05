/**Trusted types policy - not supported on all browsers */
let sanitize = null;
if (window.trustedTypes && trustedTypes.createPolicy) {
    console.log("Trusted types")
    sanitize = trustedTypes.createPolicy('default', {
        // Checking the SVG in the text or not to see the images in the find student date picker. Somehow th sanitizer removing the svg from the html.
        // DOMPurify.sanitize('<svg class="ds_icon" aria-hidden="true" role="img"><use href="/assets/images/icons/icons.stack.svg#calendar_today"></use></svg>'); this will be returning '<svg role="img" aria-hidden="true" class="ds_icon"></svg>'
        // to avoid this I'm excluding the sanity if it has a use tag in it.  https://github.com/cure53/DOMPurify
        createHTML: string => DOMPurify.sanitize(string, {RETURN_TRUSTED_TYPE: true, ADD_TAGS: ['use']}),
        createScriptURL(string) {
            let u = new URL(string, document.baseURI);
            if (u.origin === window.origin) {
                return u.href;
            }
            throw new Error('Only same-origin scripts, please');
        },
    });
}

function createScriptSourceUrlForIOS(string) {
	let u = new URL(string, document.baseURI);
	if (u.origin === window.origin) {
		return u.href;
	}
	throw new Error('Only same-origin scripts, please');

}
/**
 * The strict-dynamic keyword in WebSecurityConfig csp allows us to propagate trust to all scripts that are
 * loaded by a script that we already trust with either hashes or nonces.
 */
function dynamicallyLoadScript(type, url) {
    let script = document.createElement('script');
	if (sanitize) {
	    script.src = sanitize.createScriptURL(url);
		script.type = type;
		document.head.appendChild(script);
	} else {
		console.log("sanitize is :" + sanitize);
		script.setAttribute("src", createScriptSourceUrlForIOS(url));
		script.setAttribute("type", type);
		document.head.appendChild(script);
	}
	console.log("dynamicallyLoadScript is :" + script.src);
}

dynamicallyLoadScript('text/javascript', '/dsa/scripts/jquery-3.6.1.min.js')
dynamicallyLoadScript('module', '/dsa/scripts/design-system/dist/scripts/design-system.js')

/**Check jQuery is loaded for the following scripts*/
window.onload = function () {

    // First things first
    if (window.jQuery) {
        console.log("jQuery loaded")
    }
    if (window.DS) {
        window.DS.initAll();
        console.log("DS initialised");
    }
    let errorSummary = $("#error-summary");
    if (errorSummary.length) {
        errorSummary.focus();
    }

    // Cookie banner
    const value = `; ${document.cookie}`;
    const parts = value.split(`; cookie-notification-acknowledged=`);
    if (parts.length === 2) {
        let searchValue = parts.pop().split(';').shift();
        if (window.atob(searchValue) !== 'yes') {
            document.getElementById('cookie-notice').classList.remove('fully-hidden');
        }
    }

    // Back button
    let notFound = document.getElementById("pageNotFoundBack");
    if (notFound) {
        notFound.addEventListener("click", function () {
            window.history.back();
        });
    }

    // Vehicle Type Travel Expenses
    let noElectric = document.getElementById("no-electric");
    let noElectricGroup = document.getElementById("no-electric-group");
    let electric = document.getElementById("electric");
    let electricGroup = document.getElementById("electric-group");
    if (noElectric && electric) {
        noElectric.addEventListener("click", function () {
            noElectricGroup.style.display = "block";
            electricGroup.style.display = "none";
        });
        electric.addEventListener("click", function () {
            noElectricGroup.style.display = "none";
            electricGroup.style.display = "block";
        });
        if (electric.checked) {
            electric.click();
        } else {
            noElectric.click();
        }
    }

    // Logged in code
    if (window.loadLoggedIn) loadLoggedIn();
}
