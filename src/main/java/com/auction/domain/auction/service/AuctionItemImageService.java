package com.auction.domain.auction.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.auction.common.apipayload.status.ErrorStatus;
import com.auction.common.exception.ApiException;
import com.auction.domain.auction.dto.response.AuctionItemImageResponseDto;
import com.auction.domain.auction.entity.Auction;
import com.auction.domain.auction.entity.AuctionItemImage;
import com.auction.domain.auction.repository.AuctionItemImageRepository;
import com.auction.domain.auction.repository.AuctionRepository;
import com.auction.domain.user.entity.User;
import com.auction.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuctionItemImageService {

    private final AuctionItemImageRepository auctionItemImageRepository;
    private final AmazonS3Client amazonS3Client;
    private final AuctionRepository auctionRepository;
    private final UserService userService;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private Auction getAuctionItemWithUser(User user, Long auctionItemId) {
        return auctionRepository.findByIdAndSellerId(auctionItemId, user.getId()).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM)
        );
    }

    @Transactional
    public List<AuctionItemImageResponseDto> uploadImages(Long userId, Long auctionId, List<MultipartFile> files) throws IOException {
        User user = userService.getUser(userId);
        Auction auction = getAuctionItemWithUser(user, auctionId);

        List<AuctionItemImageResponseDto> responseDtoList = new ArrayList<>();
        List<String> supportedFileTypes = List.of("image/jpeg", "image/png", "image/gif", "image/webp", "application/pdf");

        for (MultipartFile file : files) {
            if(!supportedFileTypes.contains(file.getContentType())) {
                throw new ApiException(ErrorStatus._INVALID_IMAGE_TYPE);
            }

            // 원본 파일 이름과 확장자 추출
            String originName = file.getOriginalFilename();
            String extension = originName.substring(originName.lastIndexOf(".") + 1);

            // S3에 저장할 유니크 파일 이름 지정
            String fileName = UUID.randomUUID().toString() + "." + extension;
            String path = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/" + fileName;

            // 메타데이터 설정 및 S3 파일 업로드
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(file.getContentType());
            objectMetadata.setContentLength(file.getSize());
            amazonS3Client.putObject(bucket, fileName, file.getInputStream(), objectMetadata);

            AuctionItemImage image = AuctionItemImage.of(path, fileName, originName, extension, auction.getItem());
            AuctionItemImage savedImg = auctionItemImageRepository.save(image);
            responseDtoList.add(AuctionItemImageResponseDto.from(savedImg));
        }
        return responseDtoList;
    }

    @Transactional
    public String deleteImages(Long userId, Long auctionId, Long imageId) {
        User user = userService.getUser(userId);
        getAuctionItemWithUser(user, auctionId);
        AuctionItemImage auctionItemImage = auctionItemImageRepository.findById(imageId).orElseThrow(
                () -> new ApiException(ErrorStatus._NOT_FOUND_AUCTION_ITEM_IMAGE)
        );
        amazonS3Client.deleteObject(bucket, auctionItemImage.getFileName());
        auctionItemImageRepository.delete(auctionItemImage);

        return "이미지가 삭제되었습니다.";
    }
}
