variable "aws_region" {
  description = "AWS deployment region"
  type        = string
  default     = "ap-south-1"
}

variable "aws_profile" {
  description = "AWS CLI credentials profile"
  type        = string
  default     = "openshelf-admin"
}

variable "vpc_cidr" {
  description = "CIDR range for the OpenShelf VPC"
  type        = string
  default     = "10.20.0.0/16"
}

variable "instance_type" {
  description = "EC2 instance type for OpenShelf"
  type        = string
  default     = "t3a.xlarge"
}
