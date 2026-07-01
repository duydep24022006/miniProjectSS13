SOFTWARE REQUIREMENTS SPECIFICATION (SRS)HỆ THỐNG WEB SERVICE THƯƠNG MẠI ĐIỆN TỬ "SMART E-SHOP" (TECHNOVA)Mã tài liệu: TechNova_SRS_v1.0Chuẩn áp dụng: IEEE Std 830-1998Vai trò thực hiện: Senior Business Analyst / System Analyst1. GIỚI THIỆU (INTRODUCTION)1.1 Mục đích (Purpose)Tài liệu này cung cấp các đặc tả yêu cầu chi tiết về cả chức năng và phi chức năng cho hệ thống Backend RESTful Web Service "Smart E-Shop" của Công ty TechNova. Đối tượng sử dụng tài liệu bao gồm Giám đốc TechNova (phê duyệt nghiệp vụ), Giám đốc kỹ thuật (kiểm tra kiến trúc), Đội ngũ phát triển Backend Spring Boot (lập trình) và Đội ngũ kiểm thử (viết Test Case).1.2 Phạm vi hệ thống (Scope)Hệ thống là một giải pháp Enterprise Web Service chạy trên nền tảng Spring Boot 3.x, kết nối cơ sở dữ liệu MySQL, chịu trách nhiệm xử lý toàn bộ luồng dữ liệu mua sắm, quản lý kho và bảo mật. Hệ thống loại bỏ hoàn toàn việc quản lý thủ công bằng Excel.2. MÔ TẢ TỔNG QUAN (OVERALL DESCRIPTION)2.1 Kiến trúc tổng thể và Phân quyền (RBAC Architecture)Hệ thống áp dụng kiến trúc Stateless REST API kết hợp với mô hình bảo mật dựa trên vai trò (Role-Based Access Control - RBAC). Người dùng khi truy cập vào hệ thống bắt buộc phải đi qua bộ lọc Spring Security Filter Chain. Nếu yêu cầu xác thực hợp lệ, hệ thống sẽ cấp một chuỗi mã hóa JWT (JSON Web Token) để người dùng đính kèm vào Header của các Request tiếp theo dưới dạng Authorization: Bearer <JWT_TOKEN>.2.2 Sơ đồ Use Case Hệ thống (UML Use Case Diagram)Dưới đây là sơ đồ Use Case phân tách rõ ràng ranh giới giữa hệ thống Public (Khách hàng) và hệ thống Admin (Nhân viên/Quản lý). Khách hàng tuyệt đối không có đường truyền kết nối tới các Use Case thuộc phân hệ Admin.Đoạn mãgraph LR
%% Actors Definition
Anonymous((Khách vãng lai))
Customer((Khách hàng))
Staff((Nhân viên kho))
Manager((Quản lý))

    %% Use Cases Phân hệ Public/Customer
    subgraph Phân hệ Storefront (Public/Customer)
        UC1[Xem & Tìm kiếm sản phẩm]
        UC2[Đăng ký tài khoản]
        UC3[Đăng nhập]
        UC4[Quản lý giỏ hàng]
        UC5[Thanh toán & Đặt hàng]
    end

    %% Use Cases Phân hệ Admin
    subgraph Phân hệ Admin Portal
        UC6[Thêm/Sửa/Xóa sản phẩm]
        UC7[Xem báo cáo doanh thu]
        UC8[Cấp quyền nhân viên]
    end

    %% Mối quan hệ giữa Actor và Use Case
    Anonymous --> UC1
    Anonymous --> UC2
    Anonymous --> UC3

    Customer --> UC1
    Customer --> UC4
    Customer --> UC5

    Staff --> UC6
    
    Manager --> UC6
    Manager --> UC7
    Manager --> UC8

    %% Styling để làm nổi bật ranh giới bảo mật
    classDef public fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;
    classDef admin fill:#ffebee,stroke:#c62828,stroke-width:2px;
    class UC1,UC2,UC3,UC4,UC5 public;
    class UC6,UC7,UC8 admin;
3. ĐẶC TẢ CHI TIẾT CÁC YÊU CẦU CHỨC NĂNG (SPECIFIC REQUIREMENTS)3.1 Ma trận phân quyền API & CRUD MatrixHệ thống phải kiểm soát quyền truy cập chi tiết đến từng phương thức (Method-Level Security) của HTTP Request.Đường dẫn API (Endpoint)HTTP MethodQuyền truy cập (Allowed Roles)Mô tả chi tiết chức năng/api/v1/public/products/**GETANONYMOUS, ALLXem danh sách, tìm kiếm, xem chi tiết sản phẩm./api/v1/public/auth/registerPOSTANONYMOUSĐăng ký tài khoản mới cho Khách hàng./api/v1/public/auth/loginPOSTANONYMOUSXác thực thông tin, trả về JWT Token./api/v1/customer/cart/**GET, POST, PUT, DELETECUSTOMERXem, thêm, sửa số lượng, xóa item trong giỏ./api/v1/customer/orders/**POSTCUSTOMERTạo đơn hàng mới từ giỏ hàng./api/v1/admin/products/**POST, PUT, DELETESTAFF, MANAGERThêm, sửa thông tin, xóa mềm sản phẩm./api/v1/admin/users/**PUTMANAGERCấp/đổi quyền (Role) của nhân viên./api/v1/admin/dashboard/**GETMANAGERTruy xuất dữ liệu thống kê doanh thu tài chính.3.2 Luồng sự kiện chi tiết (Flow of Events) cho các Use Case cốt lõi3.2.1 Use Case: Đăng nhập Hệ thống (UC-03)Tác nhân kích hoạt: Tất cả người dùng (Customer, Staff, Manager).Điều kiện tiên quyết: Người dùng đã có tài khoản tồn tại trong Database.Luồng sự kiện chính (Chạy mượt):Người dùng gửi một HTTP POST request chứa username và password đến endpoint /api/v1/public/auth/login.Tầng Service tiếp nhận, dùng AuthenticationManager để xác thực thông tin.Hệ thống kiểm tra thông tin hợp lệ, thực hiện truy vấn các quyền (Roles) đi kèm của người dùng từ cơ sở dữ liệu.Hệ thống sinh mã mã hóa sinh ra một chuỗi JWT Token chứa thông tin: Subject (username), IssuedAt, ExpirationTime (24 giờ), và danh sách Roles dưới dạng Claims.Hệ thống phản hồi về mã trạng thái HTTP 200 OK kèm theo dữ liệu JSON chứa Token.Luồng ngoại lệ (Lỗi xảy ra):Sai thông tin đăng nhập: Hệ thống chặn request, trả về mã trạng thái HTTP 401 Unauthorized kèm thông điệp "Tài khoản hoặc mật khẩu không chính xác".3.2.2 Use Case: Thanh toán & Đặt hàng (UC-05) - Xử lý đồng thời (Concurrency)Tác nhân kích hoạt: Khách hàng (ROLE_CUSTOMER).Điều kiện tiên quyết: Khách hàng đã đăng nhập, giỏ hàng có ít nhất một sản phẩm hợp lệ, sản phẩm trong kho còn đủ số lượng.Luồng sự kiện chính (Chạy mượt):Khách hàng gửi HTTP POST request đến /api/v1/customer/orders.Hệ thống mở một Database Transaction (@Transactional).Hệ thống quét qua toàn bộ các item trong giỏ hàng, thực hiện kiểm tra số lượng tồn kho (stock_quantity) của từng sản phẩm trong bảng PRODUCTS.Kiểm tra thành công: Hệ thống thực hiện lệnh trừ trực tiếp số lượng tồn kho trong database: stock_quantity = stock_quantity - ordered_quantity.Hệ thống ghi dữ liệu vào bảng ORDERS và tạo các bản ghi tương ứng trong bảng ORDER_ITEMS.Hệ thống đóng Transaction, lưu dữ liệu thành công và trả về mã trạng thái 201 Created kèm thông tin đơn hàng.Luồng ngoại lệ (Lỗi xảy ra):Sản phẩm hết hàng giữa chừng: Nếu stock_quantity < ordered_quantity, hệ thống kích hoạt Rollback toàn bộ Transaction (không lưu bất kỳ dữ liệu rác nào xuống DB) và trả về mã lỗi HTTP 400 Bad Request kèm thông báo "Sản phẩm [Tên_Sản_Phẩm] đã hết hàng hoặc không đủ số lượng tồn kho".4. RÀNG BUỘC DỮ LIỆU ĐẦU VÀO (DATA VALIDATION SPECIFICATIONS)Hệ thống bắt buộc phải kiểm tra tính toàn vẹn và hợp lệ của dữ liệu ngay tại tầng DTO (Data Transfer Object) bằng thư viện spring-boot-starter-validation trước khi đưa vào xử lý logic ở tầng Service.4.1 Ràng buộc cho ProductDTO (Thêm/Sửa sản phẩm)String name: @NotBlank(message = "Tên sản phẩm không được để trống")BigDecimal price: @NotNull(message = "Giá sản phẩm không được bỏ trống"), @DecimalMin(value = "0.0", inclusive = false, message = "Giá sản phẩm phải lớn hơn 0")Integer stockQuantity: @NotNull(message = "Số lượng kho không được bỏ trống"), @Min(value = 0, message = "Số lượng tồn kho không được nhỏ hơn 0")4.2 Ràng buộc cho UserRegisterDTO (Đăng ký tài khoản)String username: @NotBlank, @Size(min = 4, max = 20, message = "Tài khoản phải từ 4 đến 20 ký tự")String password: @NotBlank, @Size(min = 6, message = "Mật khẩu phải chứa ít nhất 6 ký tự")String email: @NotBlank, @Email(message = "Định dạng Email không hợp lệ")5. SƠ ĐỒ THỰC THỂ MỐI QUAN HỆ CƠ SỞ DỮ LIỆU CHI TIẾT (ERD)Để tránh hoàn toàn lỗi N+1 Query làm suy giảm nghiêm trọng hiệu năng hệ thống khi có hàng chục ngàn lượt truy cập, cấu trúc các bảng được thiết kế chuẩn hóa và bắt buộc cấu hình nạp dữ liệu chậm (FetchType.LAZY) trong các Entity Class.Đoạn mãerDiagram
   USERS {
   bigint id PK "AUTO_INCREMENT"
   varchar username UK "NOT NULL, từ 4-20 ký tự"
   varchar password "NOT NULL, Mã hóa BCrypt"
   varchar email UK "NOT NULL"
   varchar phone "NULL"
   }

   ROLES {
   bigint id PK "AUTO_INCREMENT"
   varchar name UK "NOT NULL (ROLE_CUSTOMER, ROLE_STAFF, ROLE_MANAGER)"
   }

   USER_ROLES {
   bigint user_id FK "NOT NULL"
   bigint role_id FK "NOT NULL"
   }

   PRODUCTS {
   bigint id PK "AUTO_INCREMENT"
   varchar name "NOT NULL"
   text description "NULL"
   decimal price "NOT NULL, > 0"
   int stock_quantity "NOT NULL, >= 0"
   }

   ORDERS {
   bigint id PK "AUTO_INCREMENT"
   bigint user_id FK "NOT NULL"
   decimal total_price "NOT NULL"
   varchar status "NOT NULL (PENDING, PAID, CANCELLED)"
   timestamp created_at "DEFAULT CURRENT_TIMESTAMP"
   }

   ORDER_ITEMS {
   bigint id PK "AUTO_INCREMENT"
   bigint order_id FK "NOT NULL"
   bigint product_id FK "NOT NULL"
   int quantity "NOT NULL, > 0"
   decimal price "NOT NULL"
   }

   %% Relationships Mapping with Fetch Rules
   USERS ||--|{ USER_ROLES : "has (LAZY Fetch)"
   ROLES ||--|{ USER_ROLES : "assigned (LAZY Fetch)"
   USERS ||--|{ ORDERS : "places (LAZY Fetch)"
   ORDERS ||--|{ ORDER_ITEMS : "contains (LAZY Cascade All)"
   PRODUCTS ||--|{ ORDER_ITEMS : "referenced (LAZY Fetch)"
6. YÊU CẦU PHI CHỨC NĂNG CHI TIẾT (NON-FUNCTIONAL REQUIREMENTS)6.1 Hiệu năng và khả năng chịu tải (Performance & Scalability)NFR-01 (High Concurrency): Hệ thống Backend phải xử lý đồng thời tối thiểu 10.000 kết nối hoạt động (Concurrent Users) tại một thời điểm. Cấu hình Connection Pool của HikariCP phải thiết lập thời gian chờ kết nối connectionTimeout = 30000 (30 giây) và maximumPoolSize = 50 hoặc hơn tùy cấu hình máy chủ để tránh cạn kiệt kết nối vào MySQL.NFR-02 (Data Cache Engine): Đối với các API truy vấn đọc nhiều như /api/v1/public/products/, hệ thống bắt buộc sử dụng Redis Cache để lưu trữ tạm thời dữ liệu sản phẩm trên RAM. Khi có lượng truy cập đột biến lớn, API sẽ phản hồi ngay lập tức từ RAM của Redis với độ trễ (Latency) < 50ms, giảm tải 95% áp dụng lên cơ sở dữ liệu MySQL, ngăn chặn sập hệ thống hoàn toàn.NFR-03 (N+1 Query Prevention Constraint): Ở tầng lưu trữ dữ liệu (Repository), đối với các câu lệnh tìm kiếm thực thể liên kết (ví dụ: Tìm thông tin User lấy kèm Roles hoặc lấy Đơn hàng kèm danh sách OrderItems), nhà phát triển bắt buộc sử dụng chỉ định @EntityGraph(attributePaths = {...}) hoặc câu lệnh JOIN FETCH trong JPQL để gom tất cả dữ liệu vào 1 câu lệnh SELECT duy nhất, cấm tuyệt đối việc tạo vòng lặp SELECT gây thắt nút cổ chai.6.2 Bảo mật API nâng cao (Security & Encryption Specs)NFR-04 (Password Security): Mật khẩu người dùng được truyền lên từ DTO phải được mã hóa thông qua bộ băm mạnh BCryptPasswordEncoder với hệ số độ phức tạp (strength) được đặt mặc định là 10 trước khi lưu trữ vào trường password của bảng USERS.NFR-05 (Stateless Token Validation): Chuỗi khóa bí mật (Secret Key) để ký mã hóa JWT phải sử dụng thuật toán mã hóa tối thiểu là HS256 hoặc cao hơn và phải được cấu hình an toàn trong file application.properties dưới dạng biến môi trường, tuyệt đối không được viết trực tiếp (Hardcode) vào mã nguồn.6.3 Đặc tả chuẩn hóa cấu trúc lỗi JSON (Global Exception Handling)Để đảm bảo an toàn thông tin, hệ thống sẽ sử dụng @RestControllerAdvice kết hợp với @ExceptionHandler để bắt tất cả các ngoại lệ hệ thống và trả về đúng định dạng JSON lỗi chuẩn hóa bên dưới cho tất cả các HTTP Status Code lỗi, đặc biệt là lỗi 401 Unauthorized và 403 Forbidden.Cấu trúc JSON lỗi tiêu chuẩn của TechNova:JSON{
   "timestamp": "2026-07-01T07:15:30.123+00:00",
   "status": 403,
   "error": "Forbidden",
   "message": "Truy cập bị từ chối: Khách hàng không có quyền truy cập vào phân hệ Quản trị (Admin Portal).",
   "path": "/api/v1/admin/products"
   }