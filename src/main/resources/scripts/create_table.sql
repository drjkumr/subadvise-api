CREATE TABLE subscription (
    subscription_id CHAR(36) NOT NULL,
    subscription_name VARCHAR(255),
    subscription_date DATE,
    renewal_date DATE,
    subscription_price DECIMAL(10, 2),
    PRIMARY KEY (subscription_id)
);