output "url" {
  value = "http://${aws_lb.main.dns_name}:8080/api/v1/users"
}
