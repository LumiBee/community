jQuery( document ).ready(function() {

    $(window).scroll(function(){
    $('.topnav').toggleClass('scrollednav py-0', $(this).scrollTop() > 50);
    });
    
});

document.addEventListener('DOMContentLoaded', function () {
    const passwordSetupModalElement = document.getElementById('passwordSetupModal');

    if (passwordSetupModalElement) {
        let bsModalInstance = null;

        if (typeof bootstrap !== 'undefined' && bootstrap.Modal && typeof bootstrap.Modal.getInstance === 'function') { // Bootstrap 5+
            bsModalInstance = bootstrap.Modal.getInstance(passwordSetupModalElement) || new bootstrap.Modal(passwordSetupModalElement);
            console.log("Password setup prompt modal instance (BS5) obtained/created.");
        } else if (typeof $ !== 'undefined' && typeof $(passwordSetupModalElement).modal === 'function') { // Bootstrap 4 (jQuery)
            console.log("Password setup prompt modal using jQuery (BS4).");
        } else {
            console.warn("Bootstrap Modal JS not found for passwordSetupModal.");
            return;
        }

        if (bsModalInstance) {
            bsModalInstance.show();
            console.log("Password setup prompt modal shown via BS5 JS.");
        } else if (typeof $ !== 'undefined' && typeof $(passwordSetupModalElement).modal === 'function') {
            $(passwordSetupModalElement).modal('show');
            console.log("Password setup prompt modal shown via jQuery/BS4.");
        }

        const laterPasswordSetupBtn = document.getElementById('laterPasswordSetupBtn');
        if (laterPasswordSetupBtn) {
            laterPasswordSetupBtn.addEventListener('click', function () {
                console.log("'Later' button clicked for password setup prompt.");
                if (bsModalInstance) {
                    bsModalInstance.hide();
                } else if (typeof $ !== 'undefined' && typeof $(passwordSetupModalElement).modal === 'function') {
                    $(passwordSetupModalElement).modal('hide');
                }

                fetch('/api/user/dismiss-password-prompt', {
                    method: 'POST',
                    headers: {
                        'Content-Type': 'application/json',
                    }
                })
                    .then(response => {
                        if (response.ok) {
                            console.log("Password setup prompt dismissed on server for this session.");

                        } else {
                            response.text().then(text => console.error("Failed to dismiss password setup prompt on server:", response.status, text));
                        }
                    })
                    .catch(error => {
                        console.error("Error dismissing password setup prompt via API:", error);
                    });
            });
        }
    }
});
