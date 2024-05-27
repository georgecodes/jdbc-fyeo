resource "random_password" "db_password" {
  length           = 16
  special          = true
  override_special = "!#$%&*()-_=+[]{}<>:?"
}

# Tearing down and re-building goes wrong as secrets are scheduled for deletion
# but still kicking around. This makes the names unique
resource "random_password" "secret-suffix" {
  length           = 6
  special          = false
}

resource "aws_secretsmanager_secret" "db_user" {
  name = "apps/sandbox/dbuser-${random_password.secret-suffix.result}"
}

resource "aws_secretsmanager_secret_version" "db_user_version" {
  secret_id = aws_secretsmanager_secret.db_user.id
  secret_string = "dbuser"
}

resource "aws_secretsmanager_secret" "db_password" {
  name = "apps/sandbox/dbpassword-${random_password.secret-suffix.result}"
}

resource "aws_secretsmanager_secret_version" "db_password_version" {
  secret_id = aws_secretsmanager_secret.db_password.id
  secret_string = random_password.db_password.result
}

data "aws_iam_policy_document" "dbsecret" {
  statement {
    sid    = "EnableAnotherAWSAccountToReadTheSecret"
    effect = "Allow"

    principals {
      type        = "AWS"
      identifiers = [aws_iam_role.ecs_task_execution_role.arn]
    }

    actions   = ["secretsmanager:GetSecretValue"]
    resources = [aws_secretsmanager_secret.db_user.arn, aws_secretsmanager_secret.db_password.arn]
  }
}

resource "aws_secretsmanager_secret_policy" "dbuser" {
  secret_arn = aws_secretsmanager_secret.db_user.arn
  policy     = data.aws_iam_policy_document.dbsecret.json
}

resource "aws_secretsmanager_secret_policy" "dbpassword" {
  secret_arn = aws_secretsmanager_secret.db_password.arn
  policy     = data.aws_iam_policy_document.dbsecret.json
}