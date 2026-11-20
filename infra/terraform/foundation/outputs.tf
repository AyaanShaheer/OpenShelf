output "aws_account_id" {
  description = "Authenticated AWS account ID"
  value       = data.aws_caller_identity.current.account_id
}

output "aws_caller_arn" {
  description = "Authenticated AWS identity ARN"
  value       = data.aws_caller_identity.current.arn
}

output "aws_region" {
  description = "AWS deployment region"
  value       = data.aws_region.current.region
}

output "vpc_id" {
  description = "OpenShelf VPC ID"
  value       = aws_vpc.openshelf.id
}

output "public_subnet_ids" {
  description = "Public subnet IDs"
  value       = [for subnet in aws_subnet.public : subnet.id]
}

output "private_subnet_ids" {
  description = "Private subnet IDs"
  value       = [for subnet in aws_subnet.private : subnet.id]
}

output "availability_zones" {
  description = "Selected availability zones"
  value       = local.azs
}

output "openshelf_security_group_id" {
  description = "Security group for OpenShelf EC2 node"
  value       = aws_security_group.openshelf_node.id
}

output "ec2_instance_id" {
  description = "OpenShelf EC2 instance ID"
  value       = aws_instance.openshelf.id
}

output "ec2_public_ip" {
  description = "OpenShelf EC2 public IP"
  value       = aws_instance.openshelf.public_ip
}

output "ec2_private_ip" {
  description = "OpenShelf EC2 private IP"
  value       = aws_instance.openshelf.private_ip
}

output "ansible_ssm_bucket_name" {
  value = aws_s3_bucket.ansible_transfer.bucket
}