resource "aws_security_group" "rds" {
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port   = 5432
    to_port     = 5432
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "rds-sg"
  }
}



resource "aws_db_instance" "main" {
  allocated_storage    = 20
  engine               = "postgres"
  engine_version       = "16.2"
  instance_class       = "db.t3.micro"
  db_name              = "testdb"
  username             = aws_secretsmanager_secret_version.db_user_version.secret_string
  password             = aws_secretsmanager_secret_version.db_password_version.secret_string
  parameter_group_name = "default.postgres16"
  skip_final_snapshot  = true
  db_subnet_group_name = module.vpc.database_subnet_group_name
  vpc_security_group_ids = [aws_security_group.rds.id]

  tags = {
    Name = "main-rds-instance"
  }
}
