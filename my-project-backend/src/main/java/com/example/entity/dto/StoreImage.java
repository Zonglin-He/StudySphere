package com.example.entity.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.example.entity.BaseData;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@TableName("db_image_store")
@AllArgsConstructor
@NoArgsConstructor
public class StoreImage implements BaseData {
    @TableId(type = IdType.AUTO)
    Integer id;
    Integer uid;
    String name;
    Date time;
}
