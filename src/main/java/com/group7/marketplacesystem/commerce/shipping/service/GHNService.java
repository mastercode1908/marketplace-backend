package com.group7.marketplacesystem.commerce.shipping.service;

import com.group7.marketplacesystem.commerce.shipping.dto.ghn.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Service để gọi API GHN
 * Tất cả token và shopId phải được lấy từ database (GHNShopInfo)
 */
@Service
@RequiredArgsConstructor
public class GHNService {
    // Dùng dev URL cho môi trường test
    private static final String GHN_DEV_BASE_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api";
    private static final String GHN_DEV_CREATE_ORDER_BASE_URL = "https://dev-online-gateway.ghn.vn/shiip/public-api/v2";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Lấy danh sách tỉnh/thành phố
     * @param token Token GHN (có thể dùng token của bất kỳ seller nào để lấy master data)
     */
    public List<GHNProvince> getProvinces(String token) {
        if (restTemplate == null) {
            throw new RuntimeException("RestTemplate is not initialized");
        }
        
        String baseUrl = GHN_DEV_BASE_URL;
        String url = baseUrl + "/master-data/province";
        
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            System.out.println("Calling GHN API: " + url);
            System.out.println("Headers: " + headers);
            
            ResponseEntity<GHNProvinceResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GHNProvinceResponse.class);
            
            System.out.println("GHN Response Status: " + response.getStatusCode());
            System.out.println("GHN Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    List<GHNProvince> data = response.getBody().getData();
                    return data != null ? data : List.of();
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            // Nếu là 404, có thể endpoint sai - thử với endpoint khác
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                // Thử với endpoint không có /v2
                String altUrl = "https://dev-online-gateway.ghn.vn/shiip/public-api/master-data/province";
                System.out.println("Trying alternative URL: " + altUrl);
                try {
                    HttpHeaders headers = createHeaders(token);
                    HttpEntity<?> entity = new HttpEntity<>(headers);
                    ResponseEntity<GHNProvinceResponse> response = restTemplate.exchange(
                            altUrl, HttpMethod.GET, entity, GHNProvinceResponse.class);
                    if (response.getBody() != null && response.getBody().getCode() != null && response.getBody().getCode() == 200) {
                        List<GHNProvince> data = response.getBody().getData();
                        return data != null ? data : List.of();
                    }
                } catch (Exception ex) {
                    System.err.println("Alternative URL also failed: " + ex.getMessage());
                }
            }
            throw new RuntimeException("Error calling GHN API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách quận/huyện theo tỉnh
     * @param provinceId ID tỉnh/thành phố
     * @param token Token GHN (có thể dùng token của bất kỳ seller nào để lấy master data)
     */
    public List<GHNDistrict> getDistricts(Integer provinceId, String token) {
        String url = GHN_DEV_BASE_URL + "/master-data/district?province_id=" + provinceId;
        
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            System.out.println("Calling GHN API: " + url);
            
            ResponseEntity<GHNDistrictResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GHNDistrictResponse.class);
            
            System.out.println("GHN Response Status: " + response.getStatusCode());
            System.out.println("GHN Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    List<GHNDistrict> data = response.getBody().getData();
                    return data != null ? data : List.of();
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling GHN API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy danh sách phường/xã theo quận
     * @param districtId ID quận/huyện
     * @param token Token GHN (có thể dùng token của bất kỳ seller nào để lấy master data)
     */
    public List<GHNWard> getWards(Integer districtId, String token) {
        String url = GHN_DEV_BASE_URL + "/master-data/ward?district_id=" + districtId;
        
        try {
            HttpHeaders headers = createHeaders(token);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            System.out.println("Calling GHN API: " + url);
            
            ResponseEntity<GHNWardResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GHNWardResponse.class);
            
            System.out.println("GHN Response Status: " + response.getStatusCode());
            System.out.println("GHN Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    List<GHNWard> data = response.getBody().getData();
                    return data != null ? data : List.of();
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling GHN API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Tạo đơn hàng GHN (dùng token và shopId từ tham số)
     * @param request Request tạo đơn hàng
     * @param token Token GHN của seller
     * @param shopId Shop ID của seller
     * @return Thông tin đơn hàng đã tạo
     */
    public GHNOrderData createOrder(GHNCreateOrderRequest request, String token, Integer shopId) {
        String url = GHN_DEV_CREATE_ORDER_BASE_URL + "/shipping-order/create";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("token", token);
            headers.add("shop_id", shopId.toString());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<GHNCreateOrderRequest> entity = new HttpEntity<>(request, headers);
            
            System.out.println("Calling GHN API: " + url);
            System.out.println("Token: " + token);
            System.out.println("Shop ID: " + shopId);
            System.out.println("Request body: " + request);
            
            ResponseEntity<GHNCreateOrderResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, GHNCreateOrderResponse.class);
            
            System.out.println("GHN Response Status: " + response.getStatusCode());
            System.out.println("GHN Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    return response.getBody().getData();
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                
                // Xử lý lỗi đặc biệt: TO_ADDRESS_CONVERT_FAIL (GHN không thể convert địa chỉ)
                // Lỗi này thường do GHN sử dụng Google Maps API nhưng API key không được authorize
                // Hoặc địa chỉ không đầy đủ
                if (errorMsg != null && errorMsg.contains("TO_ADDRESS_CONVERT_FAIL")) {
                    throw new RuntimeException("GHN không thể chuyển đổi địa chỉ. " +
                            "Vui lòng kiểm tra lại địa chỉ có đầy đủ thông tin (số nhà, tên đường, phường/xã, quận/huyện, tỉnh/thành phố). " +
                            "Lỗi chi tiết: " + errorMsg);
                }
                
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling GHN API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }


    /**
     * Tính phí vận chuyển GHN (dùng token và shopId từ tham số)
     * @param request Request tính phí với thông tin địa chỉ gửi/nhận, trọng lượng
     * @param token Token GHN của seller
     * @param shopId Shop ID của seller
     * @return Phí vận chuyển (VND)
     */
    public Integer calculateShippingFee(GHNCalculateFeeRequest request, String token, Integer shopId) {
        String url = GHN_DEV_CREATE_ORDER_BASE_URL + "/shipping-order/fee";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("token", token);
            headers.add("shop_id", shopId.toString());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<GHNCalculateFeeRequest> entity = new HttpEntity<>(request, headers);
            
            System.out.println("Calling GHN API to calculate fee: " + url);
            System.out.println("Token: " + token);
            System.out.println("Shop ID: " + shopId);
            System.out.println("Request body: " + request);
            
            ResponseEntity<GHNCalculateFeeResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, GHNCalculateFeeResponse.class);
            
            System.out.println("GHN Fee Response Status: " + response.getStatusCode());
            System.out.println("GHN Fee Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    GHNCalculateFeeData data = response.getBody().getData();
                    if (data != null && data.getTotal() != null) {
                        return data.getTotal();
                    }
                    throw new RuntimeException("GHN API returned null fee data");
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling GHN fee API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }


    /**
     * Lấy chi tiết đơn hàng từ GHN
     * @param orderCode Mã đơn hàng GHN (ghn_order_code)
     * @param token Token GHN của seller
     * @param shopId Shop ID của seller
     * @return Thông tin chi tiết đơn hàng từ GHN
     */
    public GHNOrderDetailData getOrderDetail(String orderCode, String token, Integer shopId) {
        String url = GHN_DEV_CREATE_ORDER_BASE_URL + "/shipping-order/detail";
        
        try {
            HttpHeaders headers = createHeaders(token);
            headers.add("shop_id", shopId.toString());
            
            GHNOrderDetailRequest request = new GHNOrderDetailRequest();
            request.setOrderCode(orderCode);
            
            HttpEntity<GHNOrderDetailRequest> entity = new HttpEntity<>(request, headers);
            
            System.out.println("Calling GHN API to get order detail: " + url);
            System.out.println("Order code: " + orderCode);
            
            ResponseEntity<GHNOrderDetailResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, GHNOrderDetailResponse.class);
            
            System.out.println("GHN Order Detail Response Status: " + response.getStatusCode());
            System.out.println("GHN Order Detail Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    GHNOrderDetailData data = response.getBody().getData();
                    if (data != null) {
                        return data;
                    }
                    throw new RuntimeException("GHN API returned null order detail data");
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling GHN order detail API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Hủy đơn hàng GHN
     * @param orderCode Mã đơn hàng GHN (ghn_order_code)
     * @param token Token GHN của seller
     * @param shopId Shop ID của seller
     * @return true nếu hủy thành công
     */
    public boolean cancelOrder(String orderCode, String token, Integer shopId) {
        String url = GHN_DEV_CREATE_ORDER_BASE_URL + "/switch-status/cancel";
        
        try {
            HttpHeaders headers = createHeaders(token);
            headers.add("shop_id", shopId.toString());
            
            GHNCancelOrderRequest request = new GHNCancelOrderRequest();
            request.setOrderCodes(new String[]{orderCode});
            
            HttpEntity<GHNCancelOrderRequest> entity = new HttpEntity<>(request, headers);
            
            System.out.println("Calling GHN API to cancel order: " + url);
            System.out.println("Order code: " + orderCode);
            
            ResponseEntity<GHNCancelOrderResponse> response = restTemplate.exchange(
                    url, HttpMethod.POST, entity, GHNCancelOrderResponse.class);
            
            System.out.println("GHN Cancel Response Status: " + response.getStatusCode());
            System.out.println("GHN Cancel Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    GHNCancelOrderData data = response.getBody().getData();
                    return data != null && Boolean.TRUE.equals(data.getResult());
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling GHN cancel API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Lấy thông tin shop từ GHN API
     * @param token Token GHN của seller
     * @param shopId Shop ID của seller
     * @return Thông tin shop từ GHN
     */
    public GHNShopInfoData getShopInfo(String token, Integer shopId) {
        // Thử endpoint không có /v2 trước (vì một số endpoint GHN không có /v2)
        String url = GHN_DEV_CREATE_ORDER_BASE_URL + "/shop/all";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("token", token);
            headers.set("shop_id", shopId.toString());
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            System.out.println("Calling GHN API to get shop info: " + url);
            System.out.println("Token: " + token);
            System.out.println("Shop ID: " + shopId);
            
            ResponseEntity<GHNShopInfoResponse> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GHNShopInfoResponse.class);
            
            System.out.println("GHN Shop Info Response Status: " + response.getStatusCode());
            System.out.println("GHN Shop Info Response Body: " + response.getBody());
            
            if (response.getBody() != null) {
                Integer code = response.getBody().getCode();
                if (code != null && code == 200) {
                    Object dataObj = response.getBody().getData();
                    
                    if (dataObj == null) {
                        throw new RuntimeException("GHN API returned null shop info data");
                    }
                    
                    // Log để debug
                    System.out.println("Data type: " + dataObj.getClass().getName());
                    System.out.println("Data content: " + dataObj);
                    
                    // Xử lý nếu data là Map (JSON object) - endpoint /v2/shop/all trả về {last_offset, shops: [...]}
                    if (dataObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> dataMap = (Map<String, Object>) dataObj;
                        
                        // Lấy danh sách shops từ Map
                        Object shopsObj = dataMap.get("shops");
                        if (shopsObj instanceof List) {
                            @SuppressWarnings("unchecked")
                            List<Object> shopsList = (List<Object>) shopsObj;
                            
                            if (shopsList != null && !shopsList.isEmpty()) {
                                // Tìm shop có _id khớp với shopId
                                for (Object shopObj : shopsList) {
                                    if (shopObj instanceof Map) {
                                        @SuppressWarnings("unchecked")
                                        Map<String, Object> shopMap = (Map<String, Object>) shopObj;
                                        
                                        // Lấy _id từ shop (GHN dùng _id thay vì shop_id)
                                        Object idObj = shopMap.get("_id");
                                        Integer shopIdFromResponse = null;
                                        if (idObj instanceof Number) {
                                            shopIdFromResponse = ((Number) idObj).intValue();
                                        }
                                        
                                        // Nếu shop_id khớp, convert sang GHNShopInfoData
                                        if (shopIdFromResponse != null && shopIdFromResponse.equals(shopId)) {
                                            GHNShopInfoData shop = convertShopMapToShopInfoData(shopMap);
                                            if (shop != null) {
                                                return shop;
                                            }
                                        }
                                    }
                                }
                                throw new RuntimeException("Shop with ID " + shopId + " not found in GHN shop list");
                            }
                            throw new RuntimeException("GHN API returned empty shop list");
                        }
                    }
                    
                    // Xử lý nếu data là List (fallback)
                    if (dataObj instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        List<Object> shopsList = (List<Object>) dataObj;
                        if (shopsList != null && !shopsList.isEmpty()) {
                            // Convert từ Map hoặc Object sang GHNShopInfoData
                            for (Object shopObj : shopsList) {
                                GHNShopInfoData shop = convertToShopInfoData(shopObj);
                                if (shop != null && shop.getShopId() != null && shop.getShopId().equals(shopId)) {
                                    return shop;
                                }
                            }
                            throw new RuntimeException("Shop with ID " + shopId + " not found in GHN shop list");
                        }
                        throw new RuntimeException("GHN API returned empty shop list");
                    }
                    
                    // Xử lý nếu data là GHNShopInfoData
                    if (dataObj instanceof GHNShopInfoData) {
                        GHNShopInfoData data = (GHNShopInfoData) dataObj;
                        // Kiểm tra nếu shop_id khớp hoặc null (có thể là shop duy nhất)
                        if (data.getShopId() == null || data.getShopId().equals(shopId)) {
                            return data;
                        }
                        throw new RuntimeException("Shop ID mismatch. Expected: " + shopId + ", Got: " + data.getShopId());
                    }
                    
                    // Thử convert từ Map hoặc Object sang GHNShopInfoData
                    GHNShopInfoData shop = convertToShopInfoData(dataObj);
                    if (shop != null) {
                        return shop;
                    }
                    
                    throw new RuntimeException("Unexpected data type from GHN API: " + dataObj.getClass().getName());
                }
                String errorMsg = response.getBody().getMessage() != null ? 
                        response.getBody().getMessage() : "Unknown error from GHN";
                throw new RuntimeException("GHN API error: " + errorMsg + " (code: " + code + ")");
            }
            throw new RuntimeException("GHN API returned null response");
        } catch (RestClientException e) {
            System.err.println("RestClientException with URL " + url + ": " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error calling GHN shop info API: " + e.getMessage(), e);
        } catch (Exception e) {
            System.err.println("Exception: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Unexpected error: " + e.getMessage(), e);
        }
    }

    /**
     * Convert shop Map từ GHN API response sang GHNShopInfoData
     * GHN API trả về: {_id, name, phone, address, ward_code, district_id, ...}
     */
    private GHNShopInfoData convertShopMapToShopInfoData(Map<String, Object> shopMap) {
        try {
            GHNShopInfoData shop = new GHNShopInfoData();
            
            // GHN dùng _id thay vì shop_id
            if (shopMap.get("_id") != null) {
                shop.setShopId(((Number) shopMap.get("_id")).intValue());
            }
            
            // GHN dùng name thay vì shop_name
            if (shopMap.get("name") != null) {
                shop.setShopName((String) shopMap.get("name"));
            }
            
            if (shopMap.get("phone") != null) {
                shop.setPhone((String) shopMap.get("phone"));
            }
            
            // GHN có thể có address hoặc address_v2
            if (shopMap.get("address_v2") != null && !((String) shopMap.get("address_v2")).trim().isEmpty()) {
                shop.setAddress((String) shopMap.get("address_v2"));
            } else if (shopMap.get("address") != null) {
                shop.setAddress((String) shopMap.get("address"));
            }
            
            if (shopMap.get("district_id") != null) {
                shop.setDistrictId(((Number) shopMap.get("district_id")).intValue());
            }
            
            if (shopMap.get("ward_code") != null && !((String) shopMap.get("ward_code")).trim().isEmpty()) {
                shop.setWardCode((String) shopMap.get("ward_code"));
            }
            
            // GHN có thể có province_id_v2 hoặc cần tìm từ district_id
            if (shopMap.get("province_id_v2") != null) {
                Object provinceIdV2Obj = shopMap.get("province_id_v2");
                if (provinceIdV2Obj instanceof Number) {
                    Integer provinceIdV2 = ((Number) provinceIdV2Obj).intValue();
                    if (provinceIdV2 > 0) {
                        shop.setProvinceId(provinceIdV2);
                    }
                }
            }
            
            // Nếu không có province_id_v2, thử tìm từ district_id
            // Note: Cần token để gọi API, nhưng method này không có token
            // Nên bỏ qua phần này hoặc yêu cầu token từ tham số
            // Tạm thời bỏ qua vì province_id có thể lấy từ district response
            
            return shop;
        } catch (Exception e) {
            System.err.println("Error converting shop map to GHNShopInfoData: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Convert Object (Map hoặc GHNShopInfoData) sang GHNShopInfoData (fallback method)
     */
    private GHNShopInfoData convertToShopInfoData(Object obj) {
        try {
            if (obj instanceof GHNShopInfoData) {
                return (GHNShopInfoData) obj;
            }
            if (obj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) obj;
                return convertShopMapToShopInfoData(map);
            }
            // Thử dùng ObjectMapper để convert
            return objectMapper.convertValue(obj, GHNShopInfoData.class);
        } catch (Exception e) {
            System.err.println("Error converting to GHNShopInfoData: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private HttpHeaders createHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("token", token);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }
}

