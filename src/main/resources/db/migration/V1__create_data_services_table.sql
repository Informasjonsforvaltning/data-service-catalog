CREATE TABLE data_services (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    catalog_id VARCHAR(50) NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    published_date TIMESTAMP,
    data JSONB
);

-- Create indexes for better query performance
CREATE INDEX idx_data_services_catalog_id ON data_services(catalog_id);
CREATE INDEX idx_data_services_published ON data_services(published);
CREATE INDEX idx_data_services_catalog_id_published ON data_services(catalog_id, published);
