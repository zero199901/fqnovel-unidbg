package com.anjia.unidbgserver.service;

import com.anjia.unidbgserver.config.UnidbgProperties;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FQEncryptServiceTest {

    @Autowired
    private FQEncryptServiceWorker fqEncryptServiceWorker;

    @Autowired
    private UnidbgProperties properties;

    @SneakyThrows
    @Test
    void testServiceEncrypt() {
        String url = "https://api5-normal-sinfonlineb.fqnovel.com/reading/reader/batch_full/v?manifest_version_code=68132&_rticket=1754299683771&iid=4223674528611611&channel=googleplay&device_type=ANA-AN00&language=zh&host_abi=arm64-v8a&dragon_device_type=phone&resolution=1920*1080&update_version_code=68132&cdid=8d2d9eae-6ad9-4ffa-93b1-9ff11faa6b44&key_register_ts=0&pv_player=68132&os_api=32&req_type=0&dpi=480&compliance_status=0&ac=wifi&device_id=4223674528607515&os=android&os_version=13&version_code=68132&book_id=7207066781708454916&app_name=novelapp&version_name=6.8.1.32&device_brand=HUAWEI&need_personal_recommend=1&ssmix=a&player_so_load=1&item_ids=7207067126170026557&device_platform=android&is_android_pad_screen=0&aid=1967&rom_version=V417IR+release-keys";
        String formattedHeader = "accept\r\napplication/json; charset=utf-8,application/x-protobuf\r\n"
                + "cookie\r\nstore-region=cn-zj; store-region-src=did; install_id=4223674528611611;\r\n"
                + "user-agent\r\ncom.dragon.read.oversea.gp/68132 (Linux; U; Android 12; zh_CN; ANA-AN00; Build/V417IR;tt-ok/3.12.13.4-tiktok)\r\n"
                + "accept-encoding\r\ngzip\r\n"
                + "x-xs-from-web\r\n0\r\n"
                + "x-vc-bdturing-sdk-version\r\n3.7.2.cn\r\n"
                + "x-reading-request\r\n1754299683771-1106737160\r\n"
                + "sdk-version\r\n2\r\n"
                + "x-tt-store-region-src\r\ndid\r\n"
                + "x-tt-store-region\r\ncn-zj\r\n"
                + "lc\r\n101\r\n"
                + "x-ss-req-ticket\r\n1754299683771\r\n"
                + "passport-sdk-version\r\n50564\r\n"
                + "host\r\napi5-normal-sinfonlineb.fqnovel.com\r\n"
                + "connection\r\nkeep-alive";

        FQEncryptService fqEncryptService = new FQEncryptService(properties);
        Map<String, String> result = fqEncryptService.generateSignatureHeaders(url, formattedHeader);

        log.info("生成的FQ签名结果: {}", result);
    }


    @SneakyThrows
    @Test
    void testWorkerEncrypt() {
        String url = "https://api5-normal-sinfonlinec.fqnovel.com/reading/bookapi/search/tab/v?user_is_login=0&bookstore_tab=2&passback=0&channel=googleplay&device_type=ANA-AN00&language=zh&resolution=1920*1080&clicked_content=search_history&update_version_code=68132&cdid=8d2d9eae-6ad9-4ffa-93b1-9ff11faa6b44&tab_name=store&last_consume_interval=0&ac=wifi&device_id=4223674528607515&offset=0&cold_start_session_id=d433309e-2440-409c-befd-d88a15d55bf7&query=%E6%9C%AB%E4%B8%96&version_code=68132&count=20&search_source=1&battery_pct=78&sys_dark_mode=0&use_correct=true&device_platform=android&is_android_pad_screen=0&aid=1967&rom_version=V417IR+release-keys&is_first_enter_search=false&current_volume=75&manifest_version_code=68132&_rticket=1754299671111&bookshelf_search_plan=4&iid=4223674528611611&host_abi=arm64-v8a&dragon_device_type=phone&use_lynx=false&pv_player=68132&os_api=32&tab_type=3&line_words_num=0&normal_session_id=3e2492df-a5d2-4b79-bc28-c3239a3cd097&dpi=480&pad_column_cover=0&compliance_status=0&os=android&from_rs=false&os_version=13&charging=1&app_dark_mode=0&search_id=his%23%23%23%233%4020250804172750CAED531FFAF622DAD0BC&screen_brightness=72&app_name=novelapp&down_speed=89121&search_source_id=his%23%23%23&version_name=6.8.1.32&device_brand=HUAWEI&need_personal_recommend=1&ssmix=a&player_so_load=1&last_search_page_interval=0&font_scale=100&network_type=4";
        Map<String, String> headers = new HashMap<>();
        headers.put("cookie", "store-region=cn-zj; store-region-src=did; install_id=1549658252458345;");
        headers.put("accept", "application/json; charset=utf-8,application/x-protobuf");
        headers.put("x-xs-from-web", "0");
        headers.put("x-ss-req-ticket", "1754299673613");
        headers.put("x-reading-request", "1754299673613-1082356638");
        headers.put("authorization", "Bearer");
        headers.put("lc", "101");
        headers.put("x-vc-bdturing-sdk-version", "3.7.2.cn");
        headers.put("sdk-version", "2");
        headers.put("passport-sdk-version", "50564");
        headers.put("x-tt-store-region", "cn-zj");
        headers.put("x-tt-store-region-src", "did");
        headers.put("x-ss-dp","1967");
        headers.put("user-agent", "com.dragon.read.oversea.gp/68132 (Linux; U; Android 12; zh_CN; ANA-AN00; Build/V417IR;tt-ok/3.12.13.4-tiktok)");
        headers.put("accept-encoding", "gzip");

        Map<String, String> result = fqEncryptServiceWorker.generateSignatureHeaders(url, headers).get();

        log.info("生成的FQ签名结果: {}", result);
    }

}
