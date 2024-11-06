package com.auction;

import com.auction.domain.auction.entity.Item;
import com.auction.domain.auction.entity.ItemDocument;
import com.auction.domain.auction.enums.ItemCategory;
import com.auction.domain.auction.repository.ItemRepository;
import com.auction.domain.auction.service.AuctionSearchService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.datafaker.Faker;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@SpringBootTest
@Transactional
@Commit
class AuctionApplicationTests {

    @Autowired
    private ItemRepository itemRepository;
    @Autowired
    private AuctionSearchService searchService;
    @Autowired
    private ElasticsearchRepository elasticsearchRepository;

    private final String jsonFilePath = "src/main/resources/used_items_dataset_unique.json";

    @Test
    public void insert_data() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // JSON 파일에서 데이터를 읽어 리스트로 변환
            List<Map<String, String>> itemsData = objectMapper.readValue(
                    new File(jsonFilePath), new TypeReference<List<Map<String, String>>>(){});

            int batchSize = 1000;

            List<Item> items = new ArrayList<>();
            List<ItemDocument> documents = new ArrayList<>();
            for (Map<String, String> itemData : itemsData) {
                Item item = Item.of(itemData.get("name"),
                        itemData.get("description"),
                        ItemCategory.of(itemData.get("itemCategory"))
                );
                items.add(item);
                ItemDocument document = ItemDocument.from(item);
                documents.add(document);

                // 배치 사이즈에 도달할 때마다 flush 및 clear
                if (items.size() % batchSize == 0) {
                    itemRepository.saveAllAndFlush(items);
                    elasticsearchRepository.saveAll(documents);
                    items.clear();
                    documents.clear();
                }
            }

            // 남은 데이터 저장
            if(!items.isEmpty()) {
                itemRepository.saveAllAndFlush(items);
                elasticsearchRepository.saveAll(documents);
            }
            System.out.println("Batch insert completed successfully!");

        } catch (IOException e) {
            System.err.println("Failed to load data: " + e.getMessage());
        }
    }

    @Test
    public void insert_data2() {
        String[] keywords = {"삼성 갤럭시 S20 스마트폰", "애플 아이폰 11", "LG 노트북 그램", "소니 플레이스테이션 4", "삼성 갤럭시 탭 S6", "애플 아이패드 에어", "캐논 EOS 80D 카메라", "소니 A6000 미러리스 카메라", "브리츠 블루투스 스피커", "파나소닉 전자사전", "다이슨 청소기 V11", "삼성 제트 청소기", "LG 휘센 에어컨", "삼성 김치냉장고", "쿠쿠 압력밥솥", "필립스 에어프라이어", "위니아 제습기", "린나이 가스레인지", "브라운 전기면도기", "휴롬 착즙기", "이케아 침대 프레임", "한샘 4인용 소파", "시디즈 사무용 의자", "에이스 퀸 사이즈 매트리스", "리바트 원목 식탁", "한샘 책장", "이케아 드레스룸 옷장", "리클라이너 안락의자", "라자가구 3단 서랍장", "파로마 화장대 세트", "나이키 바람막이", "아디다스 후드티", "유니클로 경량 패딩", "구찌 GG 벨트", "몽클레어 다운 재킷", "노스페이스 트렌치코트", "리바이스 청바지", "아르마니 스웨터", "라코스테 피케 티셔츠", "샤넬 트위드 자켓", "킨즈 유모차", "보비 트립트랩 유아용 의자", "베이비 조거 카시트", "압소바 유아 의류 세트", "스토케 아기 침대", "페도라 아기띠", "시밀레 아기 식탁의자", "타이니러브 아기 체육관", "엘레니어 유아용 수면등", "브라이트스타트 바운서", "나이키 축구공", "아디다스 농구공", "요넥스 배드민턴 라켓", "핑골프 드라이버 클럽", "미즈노 야구 글러브", "엑스피드 덤벨 세트", "테일러메이드 골프백", "타이틀리스트 골프 공", "로드 자전거", "푸마 헬스장 가방", "해리포터 시리즈 전권", "드래곤라자 한국판", "셜록 홈즈 전집", "소설 1984 조지 오웰", "반지의 제왕 3부작", "원피스 만화책 세트", "두란노 성경", "영어 회화 교재", "수학의 정석 기본편", "사피엔스 유발 하라리", "다이슨 에어랩 스타일러", "필립스 헤어드라이어", "샤넬 루즈 코코 립스틱", "맥 스튜디오 픽스 파운데이션", "오휘 프라임 어드밴서 크림", "에스티로더 갈색병 에센스", "랑콤 아이라이너", "조말론 우드 세이지 향수", "메이블린 마스카라", "잇츠스킨 비비크림", "레고 스타워즈 세트", "타카라 토미 미니카", "바비 인형 드림하우스", "핫휠 서킷 트랙", "나노블록 피카츄", "트랜스포머 옵티머스 프라임", "플레이도우 점토 세트", "해즈브로 젠가", "푸코 테디베어 인형", "닌텐도 스위치 포켓몬", "야마하 통기타", "롤랜드 전자 드럼", "영창 업라이트 피아노", "야마하 키보드 PSR-E373", "펜더 일렉 기타", "카시오 디지털 피아노", "샘슨 콘덴서 마이크", "유니버스 바이올린", "세고비아 클래식 기타", "템버린 음악 교구", "제프리 애견 가방", "애견용 자동 급식기", "힐스 강아지 사료", "애견용 이동장", "고양이 스크래처 타워", "애견 훈련 패드", "버팔로 강아지 옷", "피플프랜드 산책용 리드줄", "고양이 터널 장난감", "오랄케어 치약 세트", "차박용 접이식 매트리스", "보쉬 와이퍼 블레이드", "블랙박스 아이나비 QXD5000", "차 내부 청소기", "주차 보조 센서", "자동차 트렁크 정리함", "타이어 공기압 측정기", "후방 카메라 시스템", "방향제 차량용 홀더", "시거잭 충전기", "시디즈 사무용 의자", "3단 서랍 캐비닛", "HP 레이저 프린터", "샤프 계산기 EL-531XH", "트윈 오버헤드 스탠드 조명", "한샘 대형 화이트보드", "필립스 미니 프로젝터", "듀얼 모니터 스탠드", "로지텍 무선 마우스", "아르테미스 펜타입 볼펜 세트", "구찌 GG 벨트", "티파니 팔찌", "샤넬 귀걸이", "롤렉스 서브마리너 시계", "까르띠에 러브링", "레이밴 선글라스", "몽블랑 볼펜", "파네라이 루미노르 마리나 시계", "샤넬 CC 펜던트 목걸이", "헬렌카민스키 모자"};

        Faker faker = new Faker(new Locale("ko-KR"));
        List<Item> items = new ArrayList<>();
        List<ItemDocument> documents = new ArrayList<>();

        for (int i = 0; i < 1000; i++) {
            for (String keyword : keywords) {
                Item item =Item.of(keyword,
                        faker.lorem().paragraph()+" "+keyword+" "+faker.lorem().paragraph(),
                        ItemCategory.DIGITAL_CONTENT);
                items.add(item);
                documents.add(ItemDocument.from(item));
            }
            itemRepository.saveAllAndFlush(items);
            elasticsearchRepository.saveAll(documents);
            items.clear();
            documents.clear();
        }

    }

    @Test
    public void get_search_time_test() {
        int count = 5;
        long sum = 0;
        String[] keyword = {"노트북", "케이스", "셔츠", "이어폰", "의자"};

        for (int i = 0; i < count; i++) {
            long start = System.currentTimeMillis();
            searchService.searchAuctionItemsByKeyword(PageRequest.of(0, 10), keyword[i]);
            long end = System.currentTimeMillis();
            sum += (end - start);
        }

        double avg = (double) sum / count;
        System.out.println("Average search time (ms): " + avg);
    }

    @Test
    public void get_elasticsearch_time_test() throws IOException {
        int count = 5;
        long sum = 0;
        String[] keyword = {"노트북", "케이스", "셔츠", "이어폰", "의자"};

        for (int i = 0; i < count; i++) {
            long start = System.currentTimeMillis();
            searchService.elasticSearchAuctionItemsByName(PageRequest.of(0, 10), keyword[i]);
            long end = System.currentTimeMillis();
            sum += (end - start);
        }

        double avg = (double) sum / count;
        System.out.println("Average search time (ms): " + avg);
    }

}
