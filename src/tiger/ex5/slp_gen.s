// Automatically generated by the Tiger compiler, do NOT edit.

	.data
slp_format:
	.string "%d "
newline:
	.string "\n"


	.text
	.globl main
main:
	pushl	%ebp
	movl	%esp, %ebp
	movl	$1, %eax
	pushl	%eax
	movl	$0, %eax
	popl	%edx
	movl	%eax, %ecx
	movl	%edx, %eax
	cltd
	cmp	$0, %ecx
	jz	.skip
	div	%ecx
.skip:
	mov	$0, %ebx
	mov	$0x1, %eax
	int	$0x80
	pushl	%eax
	pushl	$slp_format
	call	printf
	addl	$4, %esp
	pushl	$newline
	call	printf
	addl	$4, %esp
	leave
	ret

