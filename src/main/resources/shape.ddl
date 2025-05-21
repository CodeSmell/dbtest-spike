CREATE TABLE color_and_shape (
    id   				SERIAL PRIMARY KEY,
	shape       		varchar(40),
	color       		varchar(40),
    description         varchar(80),
    
	UNIQUE(shape,color)
);