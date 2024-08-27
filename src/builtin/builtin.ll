; ModuleID = 'builtin.c'
source_filename = "builtin.c"
target datalayout = "e-m:e-p:32:32-i64:64-n32-S128"
target triple = "riscv32-unknown-unknown-elf"

@.str = private unnamed_addr constant [3 x i8] c"%s\00", align 1
@.str.1 = private unnamed_addr constant [4 x i8] c"%s\0A\00", align 1
@.str.2 = private unnamed_addr constant [3 x i8] c"%d\00", align 1
@.str.3 = private unnamed_addr constant [4 x i8] c"%d\0A\00", align 1
@.str.4 = private unnamed_addr constant [5 x i8] c"true\00", align 1
@.str.5 = private unnamed_addr constant [6 x i8] c"false\00", align 1

; Function Attrs: nounwind
define dso_local void @print(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str, ptr noundef %0) #5
  ret void
}

declare dso_local i32 @printf(ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local void @println(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.1, ptr noundef %0) #5
  ret void
}

; Function Attrs: nounwind
define dso_local void @printInt(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.2, i32 noundef %0) #5
  ret void
}

; Function Attrs: nounwind
define dso_local void @printlnInt(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 (ptr, ...) @printf(ptr noundef nonnull @.str.3, i32 noundef %0) #5
  ret void
}

; Function Attrs: nounwind
define dso_local noundef ptr @getString() local_unnamed_addr #0 {
  %1 = tail call ptr @malloc(i32 noundef 1024) #5
  %2 = tail call i32 (ptr, ...) @scanf(ptr noundef nonnull @.str, ptr noundef %1) #5
  ret ptr %1
}

; Function Attrs: mustprogress nocallback nofree nosync nounwind willreturn memory(argmem: readwrite)
declare void @llvm.lifetime.start.p0(i64 immarg, ptr nocapture) #2

declare dso_local ptr @malloc(i32 noundef) local_unnamed_addr #1

declare dso_local i32 @scanf(ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: mustprogress nocallback nofree nosync nounwind willreturn memory(argmem: readwrite)
declare void @llvm.lifetime.end.p0(i64 immarg, ptr nocapture) #2

; Function Attrs: nounwind
define dso_local i32 @getInt() local_unnamed_addr #0 {
  %1 = alloca i32, align 4
  call void @llvm.lifetime.start.p0(i64 4, ptr nonnull %1) #6
  %2 = call i32 (ptr, ...) @scanf(ptr noundef nonnull @.str.2, ptr noundef nonnull %1) #5
  %3 = load i32, ptr %1, align 4, !tbaa !6
  call void @llvm.lifetime.end.p0(i64 4, ptr nonnull %1) #6
  ret i32 %3
}

; Function Attrs: nounwind
define dso_local noundef ptr @toString(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call ptr @malloc(i32 noundef 12) #5
  %3 = tail call i32 (ptr, ptr, ...) @sprintf(ptr noundef %2, ptr noundef nonnull @.str.2, i32 noundef %0) #5
  ret ptr %2
}

declare dso_local i32 @sprintf(ptr noundef, ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local ptr @array.copy(ptr noundef %0, i32 noundef %1, i32 noundef %2) local_unnamed_addr #0 {
  %4 = load i32, ptr %0, align 4, !tbaa !6
  %5 = icmp eq i32 %4, 0
  br i1 %5, label %40, label %6

6:                                                ; preds = %3
  %7 = getelementptr inbounds i8, ptr %0, i32 -4
  %8 = load i32, ptr %7, align 4, !tbaa !6
  %9 = icmp eq i32 %2, 1
  br i1 %9, label %10, label %15

10:                                               ; preds = %6
  %11 = mul i32 %8, %1
  %12 = add i32 %11, 4
  %13 = tail call ptr @malloc(i32 noundef %12) #5
  %14 = tail call ptr @memcpy(ptr noundef %13, ptr noundef nonnull %7, i32 noundef %12) #5
  br label %37

15:                                               ; preds = %6
  %16 = shl i32 %8, 2
  %17 = add i32 %16, 4
  %18 = tail call ptr @malloc(i32 noundef %17) #5
  store i32 %8, ptr %18, align 4, !tbaa !6
  %19 = getelementptr inbounds i8, ptr %18, i32 4
  %20 = icmp eq i32 %8, 0
  br i1 %20, label %37, label %21

21:                                               ; preds = %15
  %22 = add i32 %2, -1
  br label %23

23:                                               ; preds = %21, %32
  %24 = phi i32 [ 0, %21 ], [ %35, %32 ]
  %25 = getelementptr inbounds i32, ptr %0, i32 %24
  %26 = load i32, ptr %25, align 4, !tbaa !6
  %27 = icmp eq i32 %26, 0
  br i1 %27, label %32, label %28

28:                                               ; preds = %23
  %29 = inttoptr i32 %26 to ptr
  %30 = tail call ptr @array.copy(ptr noundef nonnull %29, i32 noundef %1, i32 noundef %22) #7
  %31 = ptrtoint ptr %30 to i32
  br label %32

32:                                               ; preds = %23, %28
  %33 = phi i32 [ %31, %28 ], [ 0, %23 ]
  %34 = getelementptr inbounds i32, ptr %19, i32 %24
  store i32 %33, ptr %34, align 4, !tbaa !6
  %35 = add nuw i32 %24, 1
  %36 = icmp eq i32 %35, %8
  br i1 %36, label %37, label %23, !llvm.loop !10

37:                                               ; preds = %32, %15, %10
  %38 = phi ptr [ %13, %10 ], [ %18, %15 ], [ %18, %32 ]
  %39 = getelementptr inbounds i8, ptr %38, i32 4
  br label %40

40:                                               ; preds = %3, %37
  %41 = phi ptr [ %39, %37 ], [ null, %3 ]
  ret ptr %41
}

declare dso_local ptr @memcpy(ptr noundef, ptr noundef, i32 noundef) local_unnamed_addr #1

; Function Attrs: mustprogress nofree norecurse nosync nounwind willreturn memory(argmem: read)
define dso_local i32 @array.size(ptr nocapture noundef readonly %0) local_unnamed_addr #3 {
  %2 = getelementptr inbounds i8, ptr %0, i32 -4
  %3 = load i32, ptr %2, align 4, !tbaa !6
  ret i32 %3
}

; Function Attrs: nounwind
define dso_local i32 @string.length(ptr noundef %0) local_unnamed_addr #0 {
  %2 = tail call i32 @strlen(ptr noundef %0) #5
  ret i32 %2
}

declare dso_local i32 @strlen(ptr noundef) local_unnamed_addr #1

; Function Attrs: mustprogress nofree norecurse nosync nounwind willreturn memory(argmem: read)
define dso_local i32 @string.ord(ptr nocapture noundef readonly %0, i32 noundef %1) local_unnamed_addr #3 {
  %3 = getelementptr inbounds i8, ptr %0, i32 %1
  %4 = load i8, ptr %3, align 1, !tbaa !12
  %5 = zext i8 %4 to i32
  ret i32 %5
}

; Function Attrs: nounwind
define dso_local i32 @string.parseInt(ptr noundef %0) local_unnamed_addr #0 {
  %2 = alloca i32, align 4
  call void @llvm.lifetime.start.p0(i64 4, ptr nonnull %2) #6
  %3 = call i32 (ptr, ptr, ...) @sscanf(ptr noundef %0, ptr noundef nonnull @.str.2, ptr noundef nonnull %2) #5
  %4 = load i32, ptr %2, align 4, !tbaa !6
  call void @llvm.lifetime.end.p0(i64 4, ptr nonnull %2) #6
  ret i32 %4
}

declare dso_local i32 @sscanf(ptr noundef, ptr noundef, ...) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local ptr @string.substring(ptr nocapture noundef readonly %0, i32 noundef %1, i32 noundef %2) local_unnamed_addr #0 {
  %4 = sub nsw i32 %2, %1
  %5 = add nsw i32 %4, 1
  %6 = tail call ptr @malloc(i32 noundef %5) #5
  %7 = getelementptr i8, ptr %0, i32 %1
  %8 = icmp sgt i32 %4, 0
  br i1 %8, label %11, label %9

9:                                                ; preds = %11, %3
  %10 = getelementptr inbounds i8, ptr %6, i32 %4
  store i8 0, ptr %10, align 1, !tbaa !12
  ret ptr %6

11:                                               ; preds = %3, %11
  %12 = phi i32 [ %16, %11 ], [ 0, %3 ]
  %13 = getelementptr i8, ptr %7, i32 %12
  %14 = load i8, ptr %13, align 1, !tbaa !12
  %15 = getelementptr inbounds i8, ptr %6, i32 %12
  store i8 %14, ptr %15, align 1, !tbaa !12
  %16 = add nuw nsw i32 %12, 1
  %17 = icmp eq i32 %16, %4
  br i1 %17, label %9, label %11, !llvm.loop !13
}

; Function Attrs: mustprogress nofree norecurse nosync nounwind willreturn memory(none)
define dso_local noundef nonnull ptr @builtin.bool_to_string(i1 noundef zeroext %0) local_unnamed_addr #4 {
  %2 = select i1 %0, ptr @.str.4, ptr @.str.5
  ret ptr %2
}

; Function Attrs: nounwind
define dso_local ptr @builtin.calloc(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call ptr @calloc(i32 noundef %0, i32 noundef 1) #5
  ret ptr %2
}

declare dso_local ptr @calloc(i32 noundef, i32 noundef) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local nonnull ptr @builtin.calloc_array(i32 noundef %0, i32 noundef %1) local_unnamed_addr #0 {
  %3 = mul i32 %1, %0
  %4 = add i32 %3, 4
  %5 = tail call ptr @calloc(i32 noundef %4, i32 noundef 1) #5
  store i32 %1, ptr %5, align 4, !tbaa !6
  %6 = getelementptr inbounds i8, ptr %5, i32 4
  ret ptr %6
}

; Function Attrs: nounwind
define dso_local ptr @builtin.malloc(i32 noundef %0) local_unnamed_addr #0 {
  %2 = tail call ptr @malloc(i32 noundef %0) #5
  ret ptr %2
}

; Function Attrs: nounwind
define dso_local nonnull ptr @builtin.malloc_array(i32 noundef %0, i32 noundef %1) local_unnamed_addr #0 {
  %3 = mul i32 %1, %0
  %4 = add i32 %3, 4
  %5 = tail call ptr @malloc(i32 noundef %4) #5
  store i32 %1, ptr %5, align 4, !tbaa !6
  %6 = getelementptr inbounds i8, ptr %5, i32 4
  ret ptr %6
}

; Function Attrs: nounwind
define dso_local noundef ptr @builtin.string_add(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strlen(ptr noundef %0) #5
  %4 = tail call i32 @strlen(ptr noundef %1) #5
  %5 = add i32 %4, %3
  %6 = add i32 %5, 1
  %7 = tail call ptr @malloc(i32 noundef %6) #5
  %8 = tail call ptr @strcpy(ptr noundef %7, ptr noundef %0) #5
  %9 = getelementptr inbounds i8, ptr %7, i32 %3
  %10 = tail call ptr @strcpy(ptr noundef %9, ptr noundef %1) #5
  %11 = getelementptr inbounds i8, ptr %7, i32 %5
  store i8 0, ptr %11, align 1, !tbaa !12
  ret ptr %7
}

declare dso_local ptr @strcpy(ptr noundef, ptr noundef) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_eq(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp eq i32 %3, 0
  ret i1 %4
}

declare dso_local i32 @strcmp(ptr noundef, ptr noundef) local_unnamed_addr #1

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_ge(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp sgt i32 %3, 0
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_geq(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp sgt i32 %3, -1
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_le(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp slt i32 %3, 0
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_leq(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp slt i32 %3, 1
  ret i1 %4
}

; Function Attrs: nounwind
define dso_local zeroext i1 @builtin.string_ne(ptr noundef %0, ptr noundef %1) local_unnamed_addr #0 {
  %3 = tail call i32 @strcmp(ptr noundef %0, ptr noundef %1) #5
  %4 = icmp ne i32 %3, 0
  ret i1 %4
}

attributes #0 = { nounwind "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #1 = { "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #2 = { mustprogress nocallback nofree nosync nounwind willreturn memory(argmem: readwrite) }
attributes #3 = { mustprogress nofree norecurse nosync nounwind willreturn memory(argmem: read) "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #4 = { mustprogress nofree norecurse nosync nounwind willreturn memory(none) "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" "no-trapping-math"="true" "stack-protector-buffer-size"="8" "target-cpu"="generic-rv32" "target-features"="+32bit,+a,+c,+m,+relax,+zmmul,-b,-d,-e,-experimental-smmpm,-experimental-smnpm,-experimental-ssnpm,-experimental-sspm,-experimental-ssqosid,-experimental-supm,-experimental-zacas,-experimental-zalasr,-experimental-zicfilp,-experimental-zicfiss,-f,-h,-shcounterenw,-shgatpa,-shtvala,-shvsatpa,-shvstvala,-shvstvecd,-smaia,-smcdeleg,-smcsrind,-smepmp,-smstateen,-ssaia,-ssccfg,-ssccptr,-sscofpmf,-sscounterenw,-sscsrind,-ssstateen,-ssstrict,-sstc,-sstvala,-sstvecd,-ssu64xl,-svade,-svadu,-svbare,-svinval,-svnapot,-svpbmt,-v,-xcvalu,-xcvbi,-xcvbitmanip,-xcvelw,-xcvmac,-xcvmem,-xcvsimd,-xsfcease,-xsfvcp,-xsfvfnrclipxfqf,-xsfvfwmaccqqq,-xsfvqmaccdod,-xsfvqmaccqoq,-xsifivecdiscarddlone,-xsifivecflushdlone,-xtheadba,-xtheadbb,-xtheadbs,-xtheadcmo,-xtheadcondmov,-xtheadfmemidx,-xtheadmac,-xtheadmemidx,-xtheadmempair,-xtheadsync,-xtheadvdot,-xventanacondops,-xwchc,-za128rs,-za64rs,-zaamo,-zabha,-zalrsc,-zama16b,-zawrs,-zba,-zbb,-zbc,-zbkb,-zbkc,-zbkx,-zbs,-zca,-zcb,-zcd,-zce,-zcf,-zcmop,-zcmp,-zcmt,-zdinx,-zfa,-zfbfmin,-zfh,-zfhmin,-zfinx,-zhinx,-zhinxmin,-zic64b,-zicbom,-zicbop,-zicboz,-ziccamoa,-ziccif,-zicclsm,-ziccrse,-zicntr,-zicond,-zicsr,-zifencei,-zihintntl,-zihintpause,-zihpm,-zimop,-zk,-zkn,-zknd,-zkne,-zknh,-zkr,-zks,-zksed,-zksh,-zkt,-ztso,-zvbb,-zvbc,-zve32f,-zve32x,-zve64d,-zve64f,-zve64x,-zvfbfmin,-zvfbfwma,-zvfh,-zvfhmin,-zvkb,-zvkg,-zvkn,-zvknc,-zvkned,-zvkng,-zvknha,-zvknhb,-zvks,-zvksc,-zvksed,-zvksg,-zvksh,-zvkt,-zvl1024b,-zvl128b,-zvl16384b,-zvl2048b,-zvl256b,-zvl32768b,-zvl32b,-zvl4096b,-zvl512b,-zvl64b,-zvl65536b,-zvl8192b" }
attributes #5 = { nobuiltin nounwind "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" }
attributes #6 = { nounwind }
attributes #7 = { "no-builtin-calloc" "no-builtin-malloc" "no-builtin-memcpy" "no-builtin-printf" "no-builtin-scanf" "no-builtin-sprintf" "no-builtin-sscanf" "no-builtin-strcmp" "no-builtin-strcpy" "no-builtin-strlen" }

!llvm.module.flags = !{!0, !1, !2, !4}
!llvm.ident = !{!5}

!0 = !{i32 1, !"wchar_size", i32 4}
!1 = !{i32 1, !"target-abi", !"ilp32"}
!2 = !{i32 6, !"riscv-isa", !3}
!3 = !{!"rv32i2p1_m2p0_a2p1_c2p0_zmmul1p0"}
!4 = !{i32 8, !"SmallDataLimit", i32 8}
!5 = !{!"Ubuntu clang version 20.0.0 (++20240817032242+76161451f5ca-1~exp1~20240817152407.1871)"}
!6 = !{!7, !7, i64 0}
!7 = !{!"int", !8, i64 0}
!8 = !{!"omnipotent char", !9, i64 0}
!9 = !{!"Simple C/C++ TBAA"}
!10 = distinct !{!10, !11}
!11 = !{!"llvm.loop.mustprogress"}
!12 = !{!8, !8, i64 0}
!13 = distinct !{!13, !11}
