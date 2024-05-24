provider "aws" {
  region = "eu-west-2"
}

data "aws_availability_zones" "available" {

}

data "aws_ecr_image" "app" {
  repository_name = "fyeo-spring-flyway"
  image_tag = "v1"
}

data "aws_ecr_repository" "ecr" {
  name = "fyeo-spring-flyway"
}

resource "aws_security_group" "main" {
  vpc_id = module.vpc.vpc_id

  ingress {
    from_port   = 8080
    to_port     = 8080
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
    Name = "main-sg"
  }
}

resource "aws_lb" "main" {
  name               = "main-lb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.main.id]
  subnets            = module.vpc.public_subnets

  tags = {
    Name = "main-lb"
  }
}

resource "aws_lb_target_group" "main" {
  name     = "main-tg"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = module.vpc.vpc_id
  target_type = "ip"

  health_check {
    path = "/"
    port = "8080"
  }

  lifecycle {
    create_before_destroy = true
  }

  tags = {
    Name = "main-tg"
  }
}

resource "aws_lb_listener" "main" {
  load_balancer_arn = aws_lb.main.arn
  port              = "8080"
  protocol          = "HTTP"

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.main.arn
  }
}
