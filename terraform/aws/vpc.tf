module "vpc" {
  source  = "terraform-aws-modules/vpc/aws"
  version = "5.5.3"

  name = "vpc-dev"
  cidr = "10.10.0.0/16"
  azs = ["eu-west-2a", "eu-west-2b"]
  public_subnets = ["10.10.1.0/24", "10.10.2.0/24"]
  private_subnets = ["10.10.3.0/24", "10.10.4.0/24"]

  create_database_subnet_group = true
  create_elasticache_subnet_route_table = true
  database_subnets = ["10.10.5.0/24", "10.10.6.0/24"]

  enable_nat_gateway = true
  single_nat_gateway = true

  enable_dns_hostnames = true
  enable_dns_support = true

  public_subnet_tags = {
    Type = "public-subnets"
  }
  private_subnet_tags = {
    Type = "private-subnets"
  }
  database_subnet_tags = {
    Type = "database-subnets"
  }
  tags = {
    Owner = "Anurag"
    Env = "dev"
  }
  vpc_tags = {
    Type = "vpc-dev"
  }
}