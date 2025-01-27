package newbies.handmade_mall.service;

import lombok.RequiredArgsConstructor;
import newbies.handmade_mall.dto.req.ProductDto;
import newbies.handmade_mall.dto.res.CheckoutProductViewDto;
import newbies.handmade_mall.dto.res.ProductListItemDto;
import newbies.handmade_mall.entity.Checkout;
import newbies.handmade_mall.entity.Partner;
import newbies.handmade_mall.entity.Product;
import newbies.handmade_mall.entity.ProductImage;
import newbies.handmade_mall.mapper.ProductMapper;
import newbies.handmade_mall.repository.ProductRepository;
import newbies.handmade_mall.util.Formatter;
import newbies.handmade_mall.util.SessionManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ProductCrudService {

    private final ProductRepository productRepository;

    private final ProductImageCrudService productImageCrudService;

    private final PartnerReadService partnerReadService;

    private final ProductMapper productMapper;

    private final CustomerCrudService customerCrudService;


    public CheckoutProductViewDto viewCheckoutSuccessPage(Checkout checkout, Long productId) {

        //세션 확인
        customerCrudService.getCustomer();

        Product product = getProduct(productId);

        return CheckoutProductViewDto.builder()
                                     .createdAt(Formatter.formatForViewCheckout())
                                     .productName(product.getProductName())
                                     .build();

    }

    /**
     * 제품PK로 엔티티 반환
     */
    public Product getProduct(Long productId){
        //상품 불러옴
        Optional<Product> optionalProduct = productRepository.findById(productId);

        if(optionalProduct.isEmpty()) throw new RuntimeException("해당 상품 없음");

        return optionalProduct.get();
    }


    /**
     * @return 페이지 설정 (페이지 사이즈, 정렬순서, 페이지에 들어갈 항목)
     */
    public Page<ProductListItemDto> getProductListItemDtoPage(Pageable pageable) {

        Page<Product> productPage = productRepository.findAll(pageable);

        // 각각의 Product를 ProductListViewDto로 변환
        return productPage.map(productMapper::toProductListItemDto);
    }

    /**
     * DB에 등록되어 있는 상품별 정보(이미지 외)를 수정 화면에 나타내기 위한 메서드
     *
     * @return 상품 기존 정보 DTO
     */
    public ProductDto getProductDto(Long productId) {

        //상품 PK로 찾은 상품 객체
        Product product = productRepository.findById(productId).orElseThrow(() -> new RuntimeException("상품이 존재하지 않음"));

        return productMapper.toProductDto(product);
    }

    /**
     * 상품 업데이트 사항을 DB에 저장하기 위한 메서드
     */
    public void update(ProductDto productDto) {

        // 파트너 불러오기
        String partnerAccountId = (String) SessionManager.getHttpSessionAttribute("partnerId");

        Optional<Product> optionalProduct = productRepository.findById(productDto.getProductId());

        // 상품 존재?
        if (optionalProduct.isEmpty()) return;

        // 불러온 상품
        Product foundProduct = optionalProduct.get();
        Partner foundPartner = foundProduct.getPartner();

        // 상품 수정 권한 여부?
        if (!foundPartner.getAccountId().equals(partnerAccountId)) return;

        productMapper.toEntity(productDto, foundPartner);

        //상품 엔티티에서 메인 이미지 컬럼 수정
        foundProduct.setProductImage(productImageCrudService.update(productDto, foundProduct));
    }

    /**
     * @param id soft delete 할 id
     */
    public void removeProductById(Long id) {
        Product product = productRepository.findById(id)
                                           .orElseThrow(() -> new IllegalArgumentException("찾을 수 없음 ID = " + id));

        productRepository.delete(product);
    }


    public void createByProductDto(ProductDto productDto) {

        String partnerId = (String) SessionManager.getHttpSessionAttribute("partnerId");

        Partner aPartner = partnerReadService.getByAccountId(partnerId);

        Product savedProduct = productRepository.save(productMapper.toEntity(productDto, aPartner));

        ProductImage mainProductImage = productImageCrudService.create(productDto, savedProduct);

        savedProduct.setProductImage(mainProductImage);
    }

    /**
     * 상품 수량 변경
     */
    public void updateProductCount(Long productId, Long count){
     Optional<Product> optionalProduct = productRepository.findById(productId);

     if(optionalProduct.isEmpty()) throw new RuntimeException("상품이 존재하지 않음");

     Product product = optionalProduct.get();

     product.setCount(product.getCount()-count);

     productRepository.save(product);
    }
}
