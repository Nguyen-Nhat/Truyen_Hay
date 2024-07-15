package com.ckxnhat.truyenhay.services.export_file;

import java.io.ByteArrayInputStream;

public interface ExportFileService {
    ByteArrayInputStream exportFile(String content);

    String getName();
}
