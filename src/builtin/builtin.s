	.text
	.attribute	4, 16
	.attribute	5, "rv32i2p1_m2p0_a2p1_c2p0_zmmul1p0"
	.file	"builtin.c"
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	print                           # -- Begin function print
	.p2align	1
	.type	print,@function
print:                                  # @print
# %bb.0:
	lui	a1, %hi(.L.str)
	addi	a1, a1, %lo(.L.str)
	mv	a2, a0
	mv	a0, a1
	mv	a1, a2
	tail	printf
.Lfunc_end0:
	.size	print, .Lfunc_end0-print
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	println                         # -- Begin function println
	.p2align	1
	.type	println,@function
println:                                # @println
# %bb.0:
	lui	a1, %hi(.L.str.1)
	addi	a1, a1, %lo(.L.str.1)
	mv	a2, a0
	mv	a0, a1
	mv	a1, a2
	tail	printf
.Lfunc_end1:
	.size	println, .Lfunc_end1-println
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	printInt                        # -- Begin function printInt
	.p2align	1
	.type	printInt,@function
printInt:                               # @printInt
# %bb.0:
	lui	a1, %hi(.L.str.2)
	addi	a1, a1, %lo(.L.str.2)
	mv	a2, a0
	mv	a0, a1
	mv	a1, a2
	tail	printf
.Lfunc_end2:
	.size	printInt, .Lfunc_end2-printInt
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	printlnInt                      # -- Begin function printlnInt
	.p2align	1
	.type	printlnInt,@function
printlnInt:                             # @printlnInt
# %bb.0:
	lui	a1, %hi(.L.str.3)
	addi	a1, a1, %lo(.L.str.3)
	mv	a2, a0
	mv	a0, a1
	mv	a1, a2
	tail	printf
.Lfunc_end3:
	.size	printlnInt, .Lfunc_end3-printlnInt
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	getString                       # -- Begin function getString
	.p2align	1
	.type	getString,@function
getString:                              # @getString
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	sw	s0, 8(sp)                       # 4-byte Folded Spill
	li	a0, 1024
	call	malloc
	mv	s0, a0
	lui	a0, %hi(.L.str)
	addi	a0, a0, %lo(.L.str)
	mv	a1, s0
	call	scanf
	mv	a0, s0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	lw	s0, 8(sp)                       # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end4:
	.size	getString, .Lfunc_end4-getString
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	getInt                          # -- Begin function getInt
	.p2align	1
	.type	getInt,@function
getInt:                                 # @getInt
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	lui	a0, %hi(.L.str.2)
	addi	a0, a0, %lo(.L.str.2)
	addi	a1, sp, 8
	call	scanf
	lw	a0, 8(sp)
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end5:
	.size	getInt, .Lfunc_end5-getInt
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	toString                        # -- Begin function toString
	.p2align	1
	.type	toString,@function
toString:                               # @toString
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	sw	s0, 8(sp)                       # 4-byte Folded Spill
	sw	s1, 4(sp)                       # 4-byte Folded Spill
	mv	s0, a0
	li	a0, 12
	call	malloc
	mv	s1, a0
	lui	a1, %hi(.L.str.2)
	addi	a1, a1, %lo(.L.str.2)
	mv	a2, s0
	call	sprintf
	mv	a0, s1
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	lw	s0, 8(sp)                       # 4-byte Folded Reload
	lw	s1, 4(sp)                       # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end6:
	.size	toString, .Lfunc_end6-toString
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	array.copy                      # -- Begin function array.copy
	.p2align	1
	.type	array.copy,@function
array.copy:                             # @array.copy
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)                      # 4-byte Folded Spill
	sw	s0, 24(sp)                      # 4-byte Folded Spill
	sw	s1, 20(sp)                      # 4-byte Folded Spill
	sw	s2, 16(sp)                      # 4-byte Folded Spill
	sw	s3, 12(sp)                      # 4-byte Folded Spill
	sw	s4, 8(sp)                       # 4-byte Folded Spill
	sw	s5, 4(sp)                       # 4-byte Folded Spill
	mv	s5, a0
	lw	a0, 0(a0)
	beqz	a0, .LBB7_9
# %bb.1:
	mv	s4, a2
	mv	s3, a1
	lw	s1, -4(s5)
	li	a0, 1
	bne	a2, a0, .LBB7_3
# %bb.2:
	mul	a0, s1, s3
	addi	a0, a0, 4
	call	malloc
	mv	s2, a0
	j	.LBB7_8
.LBB7_3:
	slli	a0, s1, 2
	addi	a0, a0, 4
	call	malloc
	mv	s2, a0
	sw	s1, 0(a0)
	beqz	s1, .LBB7_8
# %bb.4:
	addi	s0, s2, 4
	addi	s4, s4, -1
	j	.LBB7_6
.LBB7_5:                                #   in Loop: Header=BB7_6 Depth=1
	sw	a0, 0(s0)
	addi	s1, s1, -1
	addi	s0, s0, 4
	addi	s5, s5, 4
	beqz	s1, .LBB7_8
.LBB7_6:                                # =>This Inner Loop Header: Depth=1
	lw	a0, 0(s5)
	beqz	a0, .LBB7_5
# %bb.7:                                #   in Loop: Header=BB7_6 Depth=1
	mv	a1, s3
	mv	a2, s4
	call	array.copy
	j	.LBB7_5
.LBB7_8:
	addi	a0, s2, 4
.LBB7_9:
	lw	ra, 28(sp)                      # 4-byte Folded Reload
	lw	s0, 24(sp)                      # 4-byte Folded Reload
	lw	s1, 20(sp)                      # 4-byte Folded Reload
	lw	s2, 16(sp)                      # 4-byte Folded Reload
	lw	s3, 12(sp)                      # 4-byte Folded Reload
	lw	s4, 8(sp)                       # 4-byte Folded Reload
	lw	s5, 4(sp)                       # 4-byte Folded Reload
	addi	sp, sp, 32
	ret
.Lfunc_end7:
	.size	array.copy, .Lfunc_end7-array.copy
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	array.size                      # -- Begin function array.size
	.p2align	1
	.type	array.size,@function
array.size:                             # @array.size
# %bb.0:
	lw	a0, -4(a0)
	ret
.Lfunc_end8:
	.size	array.size, .Lfunc_end8-array.size
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	string.length                   # -- Begin function string.length
	.p2align	1
	.type	string.length,@function
string.length:                          # @string.length
# %bb.0:
	tail	strlen
.Lfunc_end9:
	.size	string.length, .Lfunc_end9-string.length
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	string.ord                      # -- Begin function string.ord
	.p2align	1
	.type	string.ord,@function
string.ord:                             # @string.ord
# %bb.0:
	add	a0, a0, a1
	lbu	a0, 0(a0)
	ret
.Lfunc_end10:
	.size	string.ord, .Lfunc_end10-string.ord
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	string.parseInt                 # -- Begin function string.parseInt
	.p2align	1
	.type	string.parseInt,@function
string.parseInt:                        # @string.parseInt
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	lui	a1, %hi(.L.str.2)
	addi	a1, a1, %lo(.L.str.2)
	addi	a2, sp, 8
	call	sscanf
	lw	a0, 8(sp)
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end11:
	.size	string.parseInt, .Lfunc_end11-string.parseInt
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	string.substring                # -- Begin function string.substring
	.p2align	1
	.type	string.substring,@function
string.substring:                       # @string.substring
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	sw	s0, 8(sp)                       # 4-byte Folded Spill
	sw	s1, 4(sp)                       # 4-byte Folded Spill
	sw	s2, 0(sp)                       # 4-byte Folded Spill
	mv	s0, a1
	mv	s2, a0
	sub	s1, a2, a1
	addi	a0, s1, 1
	call	malloc
	add	a1, a0, s1
	blez	s1, .LBB12_3
# %bb.1:                                # %.preheader
	add	s0, s0, s2
	mv	a2, a0
.LBB12_2:                               # =>This Inner Loop Header: Depth=1
	lbu	a3, 0(s0)
	sb	a3, 0(a2)
	addi	a2, a2, 1
	addi	s0, s0, 1
	bne	a2, a1, .LBB12_2
.LBB12_3:
	sb	zero, 0(a1)
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	lw	s0, 8(sp)                       # 4-byte Folded Reload
	lw	s1, 4(sp)                       # 4-byte Folded Reload
	lw	s2, 0(sp)                       # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end12:
	.size	string.substring, .Lfunc_end12-string.substring
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.bool_to_string          # -- Begin function builtin.bool_to_string
	.p2align	1
	.type	builtin.bool_to_string,@function
builtin.bool_to_string:                 # @builtin.bool_to_string
# %bb.0:
	bnez	a0, .LBB13_2
# %bb.1:
	lui	a0, %hi(.L.str.5)
	addi	a0, a0, %lo(.L.str.5)
	ret
.LBB13_2:
	lui	a0, %hi(.L.str.4)
	addi	a0, a0, %lo(.L.str.4)
	ret
.Lfunc_end13:
	.size	builtin.bool_to_string, .Lfunc_end13-builtin.bool_to_string
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.calloc                  # -- Begin function builtin.calloc
	.p2align	1
	.type	builtin.calloc,@function
builtin.calloc:                         # @builtin.calloc
# %bb.0:
	li	a1, 1
	tail	calloc
.Lfunc_end14:
	.size	builtin.calloc, .Lfunc_end14-builtin.calloc
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.calloc_array            # -- Begin function builtin.calloc_array
	.p2align	1
	.type	builtin.calloc_array,@function
builtin.calloc_array:                   # @builtin.calloc_array
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	sw	s0, 8(sp)                       # 4-byte Folded Spill
	mv	s0, a1
	mul	a0, a1, a0
	addi	a0, a0, 4
	li	a1, 1
	call	calloc
	addi	a1, a0, 4
	sw	s0, 0(a0)
	mv	a0, a1
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	lw	s0, 8(sp)                       # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end15:
	.size	builtin.calloc_array, .Lfunc_end15-builtin.calloc_array
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.malloc                  # -- Begin function builtin.malloc
	.p2align	1
	.type	builtin.malloc,@function
builtin.malloc:                         # @builtin.malloc
# %bb.0:
	tail	malloc
.Lfunc_end16:
	.size	builtin.malloc, .Lfunc_end16-builtin.malloc
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.malloc_array            # -- Begin function builtin.malloc_array
	.p2align	1
	.type	builtin.malloc_array,@function
builtin.malloc_array:                   # @builtin.malloc_array
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	sw	s0, 8(sp)                       # 4-byte Folded Spill
	mv	s0, a1
	mul	a0, a1, a0
	addi	a0, a0, 4
	call	malloc
	addi	a1, a0, 4
	sw	s0, 0(a0)
	mv	a0, a1
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	lw	s0, 8(sp)                       # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end17:
	.size	builtin.malloc_array, .Lfunc_end17-builtin.malloc_array
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.string_add              # -- Begin function builtin.string_add
	.p2align	1
	.type	builtin.string_add,@function
builtin.string_add:                     # @builtin.string_add
# %bb.0:
	addi	sp, sp, -32
	sw	ra, 28(sp)                      # 4-byte Folded Spill
	sw	s0, 24(sp)                      # 4-byte Folded Spill
	sw	s1, 20(sp)                      # 4-byte Folded Spill
	sw	s2, 16(sp)                      # 4-byte Folded Spill
	sw	s3, 12(sp)                      # 4-byte Folded Spill
	sw	s4, 8(sp)                       # 4-byte Folded Spill
	mv	s2, a1
	mv	s3, a0
	call	strlen
	mv	s0, a0
	mv	a0, s2
	call	strlen
	add	s4, a0, s0
	addi	a0, s4, 1
	call	malloc
	mv	s1, a0
	mv	a1, s3
	call	strcpy
	add	a0, s1, s0
	mv	a1, s2
	call	strcpy
	add	s4, s4, s1
	sb	zero, 0(s4)
	mv	a0, s1
	lw	ra, 28(sp)                      # 4-byte Folded Reload
	lw	s0, 24(sp)                      # 4-byte Folded Reload
	lw	s1, 20(sp)                      # 4-byte Folded Reload
	lw	s2, 16(sp)                      # 4-byte Folded Reload
	lw	s3, 12(sp)                      # 4-byte Folded Reload
	lw	s4, 8(sp)                       # 4-byte Folded Reload
	addi	sp, sp, 32
	ret
.Lfunc_end18:
	.size	builtin.string_add, .Lfunc_end18-builtin.string_add
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.string_eq               # -- Begin function builtin.string_eq
	.p2align	1
	.type	builtin.string_eq,@function
builtin.string_eq:                      # @builtin.string_eq
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	seqz	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end19:
	.size	builtin.string_eq, .Lfunc_end19-builtin.string_eq
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.string_ge               # -- Begin function builtin.string_ge
	.p2align	1
	.type	builtin.string_ge,@function
builtin.string_ge:                      # @builtin.string_ge
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	sgtz	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end20:
	.size	builtin.string_ge, .Lfunc_end20-builtin.string_ge
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.string_geq              # -- Begin function builtin.string_geq
	.p2align	1
	.type	builtin.string_geq,@function
builtin.string_geq:                     # @builtin.string_geq
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	not	a0, a0
	srli	a0, a0, 31
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end21:
	.size	builtin.string_geq, .Lfunc_end21-builtin.string_geq
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.string_le               # -- Begin function builtin.string_le
	.p2align	1
	.type	builtin.string_le,@function
builtin.string_le:                      # @builtin.string_le
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	srli	a0, a0, 31
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end22:
	.size	builtin.string_le, .Lfunc_end22-builtin.string_le
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.string_leq              # -- Begin function builtin.string_leq
	.p2align	1
	.type	builtin.string_leq,@function
builtin.string_leq:                     # @builtin.string_leq
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	slti	a0, a0, 1
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end23:
	.size	builtin.string_leq, .Lfunc_end23-builtin.string_leq
                                        # -- End function
	.option	pop
	.option	push
	.option	arch, +a, +c, +m, +zmmul
	.globl	builtin.string_ne               # -- Begin function builtin.string_ne
	.p2align	1
	.type	builtin.string_ne,@function
builtin.string_ne:                      # @builtin.string_ne
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	snez	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end24:
	.size	builtin.string_ne, .Lfunc_end24-builtin.string_ne
                                        # -- End function
	.option	pop
	.type	.L.str,@object                  # @.str
	.section	.rodata.str1.1,"aMS",@progbits,1
.L.str:
	.asciz	"%s"
	.size	.L.str, 3

	.type	.L.str.1,@object                # @.str.1
.L.str.1:
	.asciz	"%s\n"
	.size	.L.str.1, 4

	.type	.L.str.2,@object                # @.str.2
.L.str.2:
	.asciz	"%d"
	.size	.L.str.2, 3

	.type	.L.str.3,@object                # @.str.3
.L.str.3:
	.asciz	"%d\n"
	.size	.L.str.3, 4

	.type	.L.str.4,@object                # @.str.4
.L.str.4:
	.asciz	"true"
	.size	.L.str.4, 5

	.type	.L.str.5,@object                # @.str.5
.L.str.5:
	.asciz	"false"
	.size	.L.str.5, 6

	.ident	"Ubuntu clang version 20.0.0 (++20240817032242+76161451f5ca-1~exp1~20240817152407.1871)"
	.section	".note.GNU-stack","",@progbits
