# Create an ECS cluster
resource "aws_ecs_cluster" "main" {
  name = "main-cluster"
}

resource "aws_ecs_task_definition" "main" {
  family                   = "main-task"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = "512"
  memory                   = "1024"
  execution_role_arn = aws_iam_role.ecs_task_execution_role.arn
  task_role_arn = aws_iam_role.ecs_task_execution_role.arn


  container_definitions = jsonencode([
    {
      name      = "my-app"
      image = "georgemc/fyeo-aws-demo:v1"
      essential = true
      portMappings = [
        {
          containerPort = 8080
          hostPort      = 8080
        }
      ]
      environment = [
        {"name": "SPRING_DATASOURCE_URL", "value": "jdbc:secret.aws:postgresql://${aws_db_instance.main.endpoint}/${aws_db_instance.main.db_name}"},
        {"name": "SPRING_DATASOURCE_USERNAME", "value": aws_secretsmanager_secret_version.db_user_version.arn},
        {"name": "SPRING_DATASOURCE_PASSWORD", "value": aws_secretsmanager_secret_version.db_password_version.arn},
        {"name": "SPRING_FLYWAY_URL", "value": "jdbc:secret.aws:postgresql://${aws_db_instance.main.endpoint}/${aws_db_instance.main.db_name}"},
        {"name": "SPRING_FLYWAY_USER", "value": aws_secretsmanager_secret_version.db_user_version.arn},
        {"name": "SPRING_FLYWAY_PASSWORD", "value": aws_secretsmanager_secret_version.db_password_version.arn},
        {"name": "SPRING_DATASOURCE_DRIVER_CLASS_NAME", "value": "com.elevenware.jdbc.fyeo.JdbcFyeoDriver"},
        {"name": "SPRING_FLYWAY_DRIVER_CLASS_NAME", "value": "com.elevenware.jdbc.fyeo.JdbcFyeoDriver"}
      ]
      logConfiguration = {
        "logDriver": "awslogs",
        "options": {
          "awslogs-group": "/ecs/spring-flyway",
          "awslogs-region": "eu-west-2",
          "awslogs-stream-prefix": "ecs",
          "awslogs-create-group": "true"
        }
      }
    }
  ])
}

resource "aws_ecs_service" "main" {
  name            = "main-service"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.main.arn
  desired_count   = 1
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = module.vpc.public_subnets
    assign_public_ip = true
    security_groups  = [aws_security_group.main.id]
  }

  load_balancer {
    target_group_arn = aws_lb_target_group.main.arn
    container_name   = "my-app"
    container_port   = 8080
  }

  depends_on = [aws_lb_listener.main]
}
