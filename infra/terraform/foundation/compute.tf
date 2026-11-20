# Latest Ubuntu 24.04 LTS AMI
data "aws_ssm_parameter" "ubuntu_2404" {
  name = "/aws/service/canonical/ubuntu/server/24.04/stable/current/amd64/hvm/ebs-gp3/ami-id"
}

# OpenShelf EC2 instance
resource "aws_instance" "openshelf" {
  ami           = data.aws_ssm_parameter.ubuntu_2404.value
  instance_type = var.instance_type

  subnet_id = aws_subnet.public[local.azs[0]].id

  vpc_security_group_ids = [
    aws_security_group.openshelf_node.id
  ]

  iam_instance_profile = aws_iam_instance_profile.openshelf_ec2.name

  associate_public_ip_address = true

  user_data = file("${path.module}/user_data.sh")

  monitoring = false

  credit_specification {
    cpu_credits = "standard"
  }

  root_block_device {
    volume_size           = 50
    volume_type           = "gp3"
    encrypted             = true
    delete_on_termination = true
  }

  metadata_options {
    http_endpoint               = "enabled"
    http_tokens                 = "required"
    http_put_response_hop_limit = 1
  }

  tags = {
    Name = "openshelf-demo-node"
    Role = "Application-Server"
  }

  depends_on = [
    aws_iam_role_policy_attachment.ssm_core
  ]
}