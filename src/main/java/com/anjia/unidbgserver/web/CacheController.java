package com.anjia.unidbgserver.web;

import com.anjia.unidbgserver.dto.FQNovelBookInfo;
import com.anjia.unidbgserver.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@Slf4j
@RestController
@RequestMapping("/api/cache")
@RequiredArgsConstructor
public class CacheController {

    private final RedisService redisService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("/book/{bookId}/info")
    public ResponseEntity<?> getBookInfo(@PathVariable String bookId) {
        FQNovelBookInfo info = redisService.getBookInfo(bookId);
        if (info == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(info);
    }

    @GetMapping("/book/{bookId}/chapters")
    public ResponseEntity<?> getBookChapters(@PathVariable String bookId) {
        List<String> list = redisService.getChapterList(bookId);
        if (list == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("bookId", bookId);
        resp.put("count", list.size());
        resp.put("chapterIds", list);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/keys")
    public ResponseEntity<?> listKeys(@RequestParam String pattern) {
        try {
            Set<String> keys = stringRedisTemplate.keys(pattern);
            Map<String, Object> resp = new HashMap<>();
            resp.put("pattern", pattern);
            resp.put("count", keys == null ? 0 : keys.size());
            resp.put("keys", keys == null ? Collections.emptyList() : keys);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("List keys failed", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/value")
    public ResponseEntity<?> getValue(@RequestParam String key) {
        try {
            String val = stringRedisTemplate.opsForValue().get(key);
            Map<String, Object> resp = new HashMap<>();
            resp.put("key", key);
            resp.put("value", val);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Get value failed", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @GetMapping("/delete")
    public ResponseEntity<?> deleteKey(@RequestParam String key) {
        try {
            Boolean deleted = stringRedisTemplate.delete(key);
            Map<String, Object> resp = new HashMap<>();
            resp.put("key", key);
            resp.put("deleted", deleted);
            return ResponseEntity.ok(resp);
        } catch (Exception e) {
            log.error("Delete key failed", e);
            return ResponseEntity.status(500).body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}


