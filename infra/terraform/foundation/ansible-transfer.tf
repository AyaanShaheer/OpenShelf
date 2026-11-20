resource "aws_s3_bucket" "ansible_transfer" {
  bucket = "openshelf-ansible-ssm-${data.aws_caller_identity.current.account_id}-ap-south-1"

  # Dedicated temporary transfer bucket.
  force_destroy = true

  tags = {
    Name    = "openshelf-ansible-transfer"
    Purpose = "Temporary Ansible SSM transfers"
  }
}

resource "aws_s3_bucket_public_access_block" "ansible_transfer" {
  bucket = aws_s3_bucket.ansible_transfer.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_bucket_server_side_encryption_configuration" "ansible_transfer" {
  bucket = aws_s3_bucket.ansible_transfer.id

  rule {
    apply_server_side_encryption_by_default {
      sse_algorithm     = "aws:kms"
      kms_master_key_id = aws_kms_key.ansible_transfer.arn
    }
  }
}

resource "aws_kms_key" "ansible_transfer" {
  description             = "Customer-managed key for the Ansible SSM transfer bucket"
  deletion_window_in_days = 30
  enable_key_rotation     = true

  tags = {
    Name = "openshelf-ansible-transfer-key"
  }
}

resource "aws_kms_alias" "ansible_transfer" {
  name          = "alias/openshelf-ansible-transfer"
  target_key_id = aws_kms_key.ansible_transfer.arn
}

resource "aws_s3_bucket_lifecycle_configuration" "ansible_transfer" {
  bucket = aws_s3_bucket.ansible_transfer.id

  rule {
    id     = "expire-temporary-ansible-files"
    status = "Enabled"

    filter {}

    expiration {
      days = 1
    }

    abort_incomplete_multipart_upload {
      days_after_initiation = 1
    }
  }
}