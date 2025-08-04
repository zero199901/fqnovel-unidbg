package com.anjia.unidbgserver.dto;

import lombok.Data;

/**
 * FQ书籍目录请求DTO
 */
@Data
public class FQDirectoryRequest {
    
    /**
     * 书籍ID
     */
    private String bookId;
    
    /**
     * 书籍类型
     */
    private Integer bookType = 0;
    
    /**
     * 项目数据列表MD5
     */
    private String itemDataListMd5;
    
    /**
     * 目录数据MD5
     */
    private String catalogDataMd5;
    
    /**
     * 书籍信息MD5
     */
    private String bookInfoMd5;
    
    /**
     * 是否需要版本信息
     */
    private Boolean needVersion = true;
}