// Smooth scrolling for navigation links
document.querySelectorAll('a[href^="#"]').forEach(anchor => {
    anchor.addEventListener('click', function (e) {
        const target = document.querySelector(this.getAttribute('href'));
        if (!target) return;
        e.preventDefault();
        const offsetTop = target.offsetTop - 70;
        window.scrollTo({ top: offsetTop, behavior: 'smooth' });
    });
});

// Add shadow to navbar on scroll
window.addEventListener('scroll', function () {
    const navbar = document.querySelector('.navbar');
    if (!navbar) return;
    if (window.scrollY > 50) navbar.classList.add('shadow');
    else navbar.classList.remove('shadow');
});

// Chat button animation (như bản gốc)
const chatButton = document.querySelector('.position-fixed button');
if (chatButton) {
    setInterval(() => {
        chatButton.classList.add('animate__pulse');
        setTimeout(() => chatButton.classList.remove('animate__pulse'), 1000);
    }, 5000);
}
