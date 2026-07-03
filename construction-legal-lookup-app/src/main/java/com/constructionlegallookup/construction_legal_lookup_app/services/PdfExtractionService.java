package com.constructionlegallookup.construction_legal_lookup_app.services;

import java.io.IOException;
import java.io.InputStream;

public interface PdfExtractionService {
    String extractText(InputStream inputStream) throws IOException;
}
