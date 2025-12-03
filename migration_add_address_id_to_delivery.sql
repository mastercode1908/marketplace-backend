-- Migration: Thêm cột address_id vào bảng Delivery để lưu địa chỉ giao hàng
ALTER TABLE Delivery
ADD COLUMN address_id INT,
ADD CONSTRAINT fk_Delivery_BuyerAddress FOREIGN KEY (address_id) REFERENCES BuyerAddress(address_id) ON UPDATE CASCADE;













