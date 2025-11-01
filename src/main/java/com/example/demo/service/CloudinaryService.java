package com.example.demo.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryService {

    @Autowired
    private Cloudinary cloudinary;

    public Map upload(MultipartFile file, String folderName) throws IOException {
        return cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "folder", folderName,
                "use_filename", true,
                "unique_filename", false,
                "overwrite", true
        ));
    }

    public String getImageUrl(Map uploadResult) {
        return (String) uploadResult.get("secure_url");
    }

    public String getPublicId(Map uploadResult) {
        return (String) uploadResult.get("public_id");
    }
}
