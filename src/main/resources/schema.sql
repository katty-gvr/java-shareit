CREATE TABLE IF NOT EXISTS users (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(512) NOT NULL,

  CONSTRAINT pk_user PRIMARY KEY (id),
  CONSTRAINT UQ_USER_EMAIL UNIQUE (email)
);

CREATE TABLE IF NOT EXISTS items (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY NOT NULL,
  name VARCHAR(255) NOT NULL,
  description VARCHAR(2000) NOT NULL,
  available BOOLEAN NOT NULL,
  owner_id BIGINT NOT NULL,

  CONSTRAINT pk_item PRIMARY KEY(id),
  CONSTRAINT fk_item_user FOREIGN KEY(owner_id) REFERENCES users(id)
);

CREATE TABLE IF NOT EXISTS bookings (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  start_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
  end_date TIMESTAMP WITHOUT TIME ZONE,
  item_id BIGINT NOT NULL,
  booker_id BIGINT NOT NULL,
  status VARCHAR(60) DEFAULT 'WAITING',

  CONSTRAINT fk_booking_user FOREIGN KEY(booker_id) REFERENCES users(id),
  CONSTRAINT fk_booking_item FOREIGN KEY(item_id) REFERENCES items(id)
);

CREATE TABLE IF NOT EXISTS comments (
  id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
  text VARCHAR(5000) NOT NULL,
  item_id BIGINT NOT NULL,
  author_id BIGINT NOT NULL,
  created TIMESTAMP WITHOUT TIME ZONE DEFAULT now(),

  CONSTRAINT fk_comment_user FOREIGN KEY(author_id) REFERENCES users(id),
  CONSTRAINT fk_comment_item FOREIGN KEY(item_id) REFERENCES items(id)
);

