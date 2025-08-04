package com.anjia.unidbgserver.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

/**
 * FQNovel 章节内容项
 * 对应 Rust 中的 ItemContent 结构
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ItemContent {

    /**
     * 响应码
     */
    private long code;

    /**
     * 章节标题
     */
    private String title;

    /**
     * 章节内容 (加密的)
     */
    private String content;

    /**
     * 小说数据
     */
    @JsonProperty("novel_data")
    private FQNovelData novelData;

    /**
     * 文本类型
     */
    @JsonProperty("text_type")
    private long textType;

    /**
     * 加密状态
     */
    @JsonProperty("crypt_status")
    private long cryptStatus;

    /**
     * 压缩状态
     */
    @JsonProperty("compress_status")
    private long compressStatus;

    /**
     * 密钥版本
     */
    @JsonProperty("key_version")
    private long keyVersion;

    /**
     * 段落数
     */
    @JsonProperty("paragraphs_num")
    private long paragraphsNum;

    /**
     * 作者发言
     */
    @JsonProperty("author_speak")
    private String authorSpeak;
}
