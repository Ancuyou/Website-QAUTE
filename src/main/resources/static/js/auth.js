(function () {
    const form = document.getElementById('loginForm');
    if (!form) return;

    const spinner = document.getElementById('loginSpinner');
    const alertBox = document.getElementById('loginAlert');

    form.addEventListener('submit', function (e) {
        e.preventDefault();
        e.stopPropagation();
        alertBox.classList.add('d-none');

        if (!form.checkValidity()) {
            form.classList.add('was-validated');
            return;
        }

        spinner.classList.remove('d-none');

        // Demo giả lập API login – thay bằng fetch('/api/auth/login', {...})
        setTimeout(() => {
            spinner.classList.add('d-none');

            const username = document.getElementById('loginUsername').value;
            const pass = document.getElementById('loginPassword').value;

            // Demo rule: username có '@' và pass >= 6 mới thành công
            if (username.includes('@') && pass.length >= 6) {
                const modalEl = document.getElementById('loginModal');
                const modal = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                modal.hide();

                // TODO: sau này cập nhật navbar bằng tên user…
            } else {
                alertBox.textContent = 'Thông tin đăng nhập chưa đúng.';
                alertBox.classList.remove('d-none');
            }
        }, 800);
    }, false);
})();
