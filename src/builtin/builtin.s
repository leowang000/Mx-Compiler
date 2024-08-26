	.text
	.attribute	4, 16
	.attribute	5, "rv32i2p1_m2p0_a2p1_c2p0_zmmul1p0"
	.file	"builtin.c"
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
	.globl	__mx_array_copy                 # -- Begin function __mx_array_copy
	.p2align	1
	.type	__mx_array_copy,@function
__mx_array_copy:                        # @__mx_array_copy
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
	addi	s0, s0, 4
	addi	s1, s1, -1
	addi	s5, s5, 4
	beqz	s1, .LBB7_8
.LBB7_6:                                # =>This Inner Loop Header: Depth=1
	lw	a0, 0(s5)
	beqz	a0, .LBB7_5
# %bb.7:                                #   in Loop: Header=BB7_6 Depth=1
	mv	a1, s3
	mv	a2, s4
	call	__mx_array_copy
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
	.size	__mx_array_copy, .Lfunc_end7-__mx_array_copy
                                        # -- End function
	.globl	__mx_array_size                 # -- Begin function __mx_array_size
	.p2align	1
	.type	__mx_array_size,@function
__mx_array_size:                        # @__mx_array_size
# %bb.0:
	lw	a0, -4(a0)
	ret
.Lfunc_end8:
	.size	__mx_array_size, .Lfunc_end8-__mx_array_size
                                        # -- End function
	.globl	__mx_string_length              # -- Begin function __mx_string_length
	.p2align	1
	.type	__mx_string_length,@function
__mx_string_length:                     # @__mx_string_length
# %bb.0:
	tail	strlen
.Lfunc_end9:
	.size	__mx_string_length, .Lfunc_end9-__mx_string_length
                                        # -- End function
	.globl	__mx_string_ord                 # -- Begin function __mx_string_ord
	.p2align	1
	.type	__mx_string_ord,@function
__mx_string_ord:                        # @__mx_string_ord
# %bb.0:
	add	a0, a0, a1
	lbu	a0, 0(a0)
	ret
.Lfunc_end10:
	.size	__mx_string_ord, .Lfunc_end10-__mx_string_ord
                                        # -- End function
	.globl	__mx_string_parseInt            # -- Begin function __mx_string_parseInt
	.p2align	1
	.type	__mx_string_parseInt,@function
__mx_string_parseInt:                   # @__mx_string_parseInt
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
	.size	__mx_string_parseInt, .Lfunc_end11-__mx_string_parseInt
                                        # -- End function
	.globl	__mx_string_substring           # -- Begin function __mx_string_substring
	.p2align	1
	.type	__mx_string_substring,@function
__mx_string_substring:                  # @__mx_string_substring
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
# %bb.1:
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
	.size	__mx_string_substring, .Lfunc_end12-__mx_string_substring
                                        # -- End function
	.globl	__mx_builtin_bool_to_string     # -- Begin function __mx_builtin_bool_to_string
	.p2align	1
	.type	__mx_builtin_bool_to_string,@function
__mx_builtin_bool_to_string:            # @__mx_builtin_bool_to_string
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
	.size	__mx_builtin_bool_to_string, .Lfunc_end13-__mx_builtin_bool_to_string
                                        # -- End function
	.globl	__mx_builtin_calloc             # -- Begin function __mx_builtin_calloc
	.p2align	1
	.type	__mx_builtin_calloc,@function
__mx_builtin_calloc:                    # @__mx_builtin_calloc
# %bb.0:
	li	a1, 1
	tail	calloc
.Lfunc_end14:
	.size	__mx_builtin_calloc, .Lfunc_end14-__mx_builtin_calloc
                                        # -- End function
	.globl	__mx_builtin_calloc_array       # -- Begin function __mx_builtin_calloc_array
	.p2align	1
	.type	__mx_builtin_calloc_array,@function
__mx_builtin_calloc_array:              # @__mx_builtin_calloc_array
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
	.size	__mx_builtin_calloc_array, .Lfunc_end15-__mx_builtin_calloc_array
                                        # -- End function
	.globl	__mx_builtin_malloc             # -- Begin function __mx_builtin_malloc
	.p2align	1
	.type	__mx_builtin_malloc,@function
__mx_builtin_malloc:                    # @__mx_builtin_malloc
# %bb.0:
	tail	malloc
.Lfunc_end16:
	.size	__mx_builtin_malloc, .Lfunc_end16-__mx_builtin_malloc
                                        # -- End function
	.globl	__mx_builtin_malloc_array       # -- Begin function __mx_builtin_malloc_array
	.p2align	1
	.type	__mx_builtin_malloc_array,@function
__mx_builtin_malloc_array:              # @__mx_builtin_malloc_array
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
	.size	__mx_builtin_malloc_array, .Lfunc_end17-__mx_builtin_malloc_array
                                        # -- End function
	.globl	__mx_builtin_string_add         # -- Begin function __mx_builtin_string_add
	.p2align	1
	.type	__mx_builtin_string_add,@function
__mx_builtin_string_add:                # @__mx_builtin_string_add
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
	.size	__mx_builtin_string_add, .Lfunc_end18-__mx_builtin_string_add
                                        # -- End function
	.globl	__mx_builtin_string_eq          # -- Begin function __mx_builtin_string_eq
	.p2align	1
	.type	__mx_builtin_string_eq,@function
__mx_builtin_string_eq:                 # @__mx_builtin_string_eq
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	seqz	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end19:
	.size	__mx_builtin_string_eq, .Lfunc_end19-__mx_builtin_string_eq
                                        # -- End function
	.globl	__mx_builtin_string_ge          # -- Begin function __mx_builtin_string_ge
	.p2align	1
	.type	__mx_builtin_string_ge,@function
__mx_builtin_string_ge:                 # @__mx_builtin_string_ge
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	sgtz	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end20:
	.size	__mx_builtin_string_ge, .Lfunc_end20-__mx_builtin_string_ge
                                        # -- End function
	.globl	__mx_builtin_string_geq         # -- Begin function __mx_builtin_string_geq
	.p2align	1
	.type	__mx_builtin_string_geq,@function
__mx_builtin_string_geq:                # @__mx_builtin_string_geq
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
	.size	__mx_builtin_string_geq, .Lfunc_end21-__mx_builtin_string_geq
                                        # -- End function
	.globl	__mx_builtin_string_le          # -- Begin function __mx_builtin_string_le
	.p2align	1
	.type	__mx_builtin_string_le,@function
__mx_builtin_string_le:                 # @__mx_builtin_string_le
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	srli	a0, a0, 31
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end22:
	.size	__mx_builtin_string_le, .Lfunc_end22-__mx_builtin_string_le
                                        # -- End function
	.globl	__mx_builtin_string_leq         # -- Begin function __mx_builtin_string_leq
	.p2align	1
	.type	__mx_builtin_string_leq,@function
__mx_builtin_string_leq:                # @__mx_builtin_string_leq
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	slti	a0, a0, 1
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end23:
	.size	__mx_builtin_string_leq, .Lfunc_end23-__mx_builtin_string_leq
                                        # -- End function
	.globl	__mx_builtin_string_ne          # -- Begin function __mx_builtin_string_ne
	.p2align	1
	.type	__mx_builtin_string_ne,@function
__mx_builtin_string_ne:                 # @__mx_builtin_string_ne
# %bb.0:
	addi	sp, sp, -16
	sw	ra, 12(sp)                      # 4-byte Folded Spill
	call	strcmp
	snez	a0, a0
	lw	ra, 12(sp)                      # 4-byte Folded Reload
	addi	sp, sp, 16
	ret
.Lfunc_end24:
	.size	__mx_builtin_string_ne, .Lfunc_end24-__mx_builtin_string_ne
                                        # -- End function
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
	.addrsig
