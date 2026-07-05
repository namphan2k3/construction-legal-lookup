# Construction Legal Lookup

Hệ thống tra cứu văn bản pháp luật ngành xây dựng tích hợp AI.

## Tính năng chính

- 🔍 **Tra cứu văn bản**: Full-text search, bộ lọc theo loại, cơ quan ban hành, năm, trạng thái hiệu lực
- 📚 **Xem chi tiết**: Nội dung, liên kết đến PDF
- 💾 **Bookmark & lịch sử**: Lưu văn bản yêu thích, xem lịch sử tìm kiếm/xem
- 🤖 **AI hỗ trợ**: Tóm tắt, hỏi đáp, giải thích nội dung (Gemini)
- 📊 **Admin dashboard**: Quản lý văn bản, user, thống kê hoạt động

## Công nghệ sử dụng

- **Backend**: Spring Boot 3.x, Spring Security, Spring Data JPA
- **Database**: MySQL 8.x (Flyway migrations)
- **Cache & Rate Limiting**: Redis 7.x
- **Tích hợp AI**: Google Gemini
- **Cloud Storage**: Cloudinary (PDF)
- **Containerization**: Docker, Docker Compose

## Bắt đầu

### Yêu cầu

- Java 17+
- Maven 3.9+
- Docker & Docker Compose (để chạy MySQL & Redis)

### Cài đặt & chạy

1. **Clone repo**:
   ```bash
   git clone <repo-url>
   cd ConstructionLegalLookup
   ```

2. **Khởi động MySQL & Redis**:
   ```bash
   docker-compose up -d
   ```

3. **Cấu hình môi trường** (nếu cần):
   - Copy `construction-legal-lookup-app/src/main/resources/application-dev.yaml.example` (nếu có) và cập nhật cấu hình
   - API key cho Gemini và Cloudinary được đặt trong `application-dev.yaml`

4. **Chạy ứng dụng**:
   ```bash
   cd construction-legal-lookup-app
   mvn spring-boot:run
   ```

5. **Truy cập**:
   - API: `http://localhost:8080/api`
   - Swagger UI: `http://localhost:8080/api/swagger-ui/index.html`

## Crawler & Dữ liệu mẫu

Dự án sử dụng chiến lược **crawl 1 lần → lưu JSON → Flyway migration** để import dữ liệu vào DB, không cần deploy crawler lên production.

### Cấu trúc thư mục
```
crawler/
├── data/
│   ├── sample_schema.json    # Schema mẫu cho dữ liệu crawl
│   ├── sample_data.json      # Dữ liệu mẫu để test
│   └── crawled_data.json     # Dữ liệu sau khi chạy crawler
├── main.py                   # Crawler chính (VBPL)
└── requirements.txt          # Dependencies Python
```

### Chạy crawler
1. Cài đặt dependencies:
   ```bash
   cd crawler
   pip install -r requirements.txt
   ```

2. Chạy crawler (lấy dữ liệu từ VBPL):
   ```bash
   python main.py
   ```

3. Sau khi crawl xong, dữ liệu sẽ được lưu vào `data/crawled_data.json`. Bạn có thể chỉnh sửa, bổ sung dữ liệu này trước khi tạo migration.

### Migration dữ liệu
- Dữ liệu mẫu đã được tạo sẵn trong `V4__seed_documents.sql`.
- Khi ứng dụng khởi động, Flyway sẽ tự động chạy migration và import dữ liệu vào DB.
- Nếu bạn có dữ liệu riêng từ crawler, hãy tạo migration mới (V5, V6,...) để import dữ liệu của mình.

## Tài khoản demo

- [API Documentation](docs/API.md)
- [Database Design](docs/DATABASE.md)

## Đóng góp

Mọi đóng góp đều được chào đón!

## License

MIT
