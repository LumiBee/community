ALTER table portfolio
    drop column cover_img;

ALTER table portfolio
add column cover_img_url varchar(255);