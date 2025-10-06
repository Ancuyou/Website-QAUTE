/**
 * chat-bubble.js - JavaScript điều khiển chat bubble kiểu Facebook
 */
document.addEventListener('DOMContentLoaded', function() {
    // Các phần tử chat
    const chatLauncher = document.getElementById('chat-launcher');
    const chatListBubble = document.getElementById('chat-list-bubble');
    const chatBubble = document.getElementById('chat-bubble');
    const minimizeChatList = document.getElementById('minimize-chat-list');
    const closeChatList = document.getElementById('close-chat-list');
    const minimizeChat = document.getElementById('minimize-chat');
    const closeChat = document.getElementById('close-chat');
    const chatContacts = document.querySelectorAll('.chat-contact');
    
    // Biến theo dõi trạng thái
    let activeChatBubbleId = null;
    
    // Xử lý khi nhấn vào nút chat launcher
    chatLauncher.addEventListener('click', function() {
        // Nếu chat bubble đang mở thì đóng nó
        if (!chatListBubble.classList.contains('d-none') || !chatBubble.classList.contains('d-none')) {
            chatListBubble.classList.add('d-none');
            chatBubble.classList.add('d-none');
        } else {
            // Mặc định mở danh sách chat
            chatListBubble.classList.remove('d-none');
        }
    });
    
    // Xử lý khi nhấn vào nút minimize của danh sách chat
    minimizeChatList.addEventListener('click', function() {
        chatListBubble.classList.add('d-none');
    });
    
    // Xử lý khi nhấn vào nút close của danh sách chat
    closeChatList.addEventListener('click', function() {
        chatListBubble.classList.add('d-none');
    });
    
    // Xử lý khi nhấn vào nút minimize của chat bubble
    minimizeChat.addEventListener('click', function() {
        chatBubble.classList.add('d-none');
    });
    
    // Xử lý khi nhấn vào nút close của chat bubble
    closeChat.addEventListener('click', function() {
        chatBubble.classList.add('d-none');
        activeChatBubbleId = null;
    });
    
    // Xử lý khi nhấn vào một liên hệ trong danh sách chat
    chatContacts.forEach(function(contact) {
        contact.addEventListener('click', function(e) {
            e.preventDefault();
            
            // Lấy ID của tư vấn viên
            const consultantId = this.getAttribute('data-consultant');
            const consultantName = this.querySelector('h6').textContent;
            const consultantImg = this.querySelector('img').src;
            
            // Cập nhật tên và ảnh trong chat bubble
            document.getElementById('chat-bubble-name').textContent = consultantName;
            document.querySelector('#chat-bubble .card-header img').src = consultantImg;
            
            // Lưu ID của chat bubble đang hoạt động
            activeChatBubbleId = consultantId;
            
            // Ẩn danh sách chat và hiển thị chat bubble
            chatListBubble.classList.add('d-none');
            chatBubble.classList.remove('d-none');
            
            // Cuộn xuống cuối cùng trong khung chat
            const chatMessages = document.getElementById('chat-messages');
            chatMessages.scrollTop = chatMessages.scrollHeight;
        });
    });
    
    // Xử lý khi nhấn vào liên kết "Chat với tư vấn viên" trong menu
    const chatMenuLinks = document.querySelectorAll('a[href="#chat"]');
    chatMenuLinks.forEach(function(link) {
        link.addEventListener('click', function(e) {
            // Mở danh sách chat khi nhấn vào liên kết trong menu
            chatListBubble.classList.remove('d-none');
            chatBubble.classList.add('d-none');
        });
    });
    
    // Xử lý khi click vào nút gửi tin nhắn
    const sendButton = document.querySelector('#chat-bubble .btn-primary');
    const messageInput = document.querySelector('#chat-bubble input.form-control');
    
    sendButton.addEventListener('click', sendMessage);
    messageInput.addEventListener('keypress', function(e) {
        if (e.key === 'Enter') {
            sendMessage();
        }
    });
    
    function sendMessage() {
        const message = messageInput.value.trim();
        if (message === '') return;
        
        // Lấy thời gian hiện tại
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const timeString = `${hours}:${minutes}`;
        
        // Tạo phần tử tin nhắn mới
        const chatMessages = document.getElementById('chat-messages');
        const newMessage = document.createElement('div');
        newMessage.className = 'd-flex justify-content-end mb-3';
        newMessage.innerHTML = `
            <div class="bg-primary text-white rounded py-2 px-3 shadow-sm">
                <p class="mb-0">${message}</p>
                <small class="text-white-50 mt-1 d-block text-end">${timeString}</small>
            </div>
            <img src="https://via.placeholder.com/32" class="rounded-circle align-self-end ms-2" alt="You" width="32" height="32">
        `;
        
        // Thêm tin nhắn vào khung chat
        chatMessages.appendChild(newMessage);
        
        // Xóa nội dung input
        messageInput.value = '';
        
        // Cuộn xuống cuối cùng
        chatMessages.scrollTop = chatMessages.scrollHeight;
        
        // Hiển thị typing indicator
        setTimeout(showTypingIndicator, 500);
        
        // Mô phỏng phản hồi từ tư vấn viên
        setTimeout(simulateReply, 2000);
    }
    
    function showTypingIndicator() {
        const typingIndicator = document.querySelector('.typing-indicator');
        typingIndicator.style.display = 'flex';
        
        // Cuộn xuống để hiển thị typing indicator
        const chatMessages = document.getElementById('chat-messages');
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }
    
    function simulateReply() {
        // Ẩn typing indicator
        const typingIndicator = document.querySelector('.typing-indicator');
        typingIndicator.style.display = 'none';
        
        // Các câu trả lời mẫu
        const replies = [
            "Cảm ơn bạn đã chia sẻ thông tin. Tôi sẽ giúp bạn tìm hiểu thêm về các chuyên ngành phù hợp.",
            "Dựa trên thông tin bạn cung cấp, tôi nghĩ bạn nên cân nhắc các lĩnh vực phù hợp với thế mạnh của mình.",
            "Bạn có thể cho tôi biết thêm về sở thích và điểm mạnh của bạn không?",
            "Nếu bạn quan tâm đến lĩnh vực này, tôi có thể giới thiệu một số khóa học phù hợp.",
            "Tôi đề xuất bạn nên tham khảo thêm ý kiến từ các sinh viên đã học chuyên ngành đó."
        ];
        
        // Chọn ngẫu nhiên một câu trả lời
        const randomReply = replies[Math.floor(Math.random() * replies.length)];
        
        // Lấy thời gian hiện tại
        const now = new Date();
        const hours = String(now.getHours()).padStart(2, '0');
        const minutes = String(now.getMinutes()).padStart(2, '0');
        const timeString = `${hours}:${minutes}`;
        
        // Tạo phần tử tin nhắn phản hồi
        const chatMessages = document.getElementById('chat-messages');
        const replyMessage = document.createElement('div');
        replyMessage.className = 'd-flex mb-3';
        replyMessage.innerHTML = `
            <img src="https://via.placeholder.com/32" class="rounded-circle align-self-end me-2" alt="Consultant" width="32" height="32">
            <div class="bg-white rounded py-2 px-3 mr-3 shadow-sm">
                <p class="mb-0">${randomReply}</p>
                <small class="text-muted mt-1 d-block">${timeString}</small>
            </div>
        `;
        
        // Thêm tin nhắn vào khung chat
        chatMessages.appendChild(replyMessage);
        
        // Cuộn xuống cuối cùng
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    // Ẩn typing indicator ban đầu
    document.querySelector('.typing-indicator').style.display = 'none';
});