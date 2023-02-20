//======================================================================
//
//                              RAW DATA
//
//======================================================================

## Measurements
Taken on lnxsrv13.seas.ucla.edu on Sunday February 19, 16:04h

input=/usr/local/cs/graalvm-ce-java17-22.3.1/lib/modules

BASELINE: GZIP

time gzip <$input >gzip.gz         
     | Trial 1   Trial 2   Trial 3 
-----------------------------------
real | 0m9.271s  0m9.253s  0m9.257s
user | 0m9.127s  0m9.121s  0m9.112s
sys  | 0m0.082s  0m0.086s  0m0.094s


Parallel Implementations of GZIP
//======================================================================
*         Default Runs                          8 Threads
*/

pigz
//**********************************************************************
time pigz <$input >pigz.gz           time pigz -p 8 <$input >pigz.gz           
     | Trial 1   Trial 2   Trial 3        | Trial 1   Trial 2   Trial 3   
-----------------------------------  -----------------------------------  
real | 0m2.573s  0m2.564s  0m2.649s  real | 0m2.818s  0m3.342s  0m2.765s
user | 0m9.087s  0m9.076s  0m9.081s  user | 0m9.190s  0m9.238s  0m9.199s
sys  | 0m0.140s  0m0.170s  0m0.153s  sys  | 0m0.240s  0m0.156s  0m0.200s


Pigzj
//**********************************************************************
time java Pigzj <$input >Pigzj.gz    time java Pigzj -8 <$input >Pigzj.gz    
     | Trial 1   Trial 2   Trial 3        | Trial 1   Trial 2   Trial 3   
-----------------------------------  -----------------------------------  
real | 0m2.929s  0m2.892s  0m2.900s  real | 0m3.014s  0m2.990s  0m3.002s
user | 0m9.289s  0m9.285s  0m9.309s  user | 0m9.258s  0m9.351s  0m9.334s
sys  | 0m0.294s  0m0.269s  0m0.293s  sys  | 0m0.323s  0m0.274s  0m0.280s


native-image Pigzj
//**********************************************************************
time ./pigzj <$input >pigzj.gz       time ./pigzj -8 <$input >pigzj.gz       
     | Trial 1   Trial 2   Trial 3        | Trial 1   Trial 2   Trial 3   
-----------------------------------  -----------------------------------  
real | 0m3.148s  0m2.722s  0m2.778s  real | 0m2.806s  0m2.931s  0m2.841s
user | 0m9.194s  0m9.192s  0m9.199s  user | 0m9.226s  0m9.227s  0m9.178s
sys  | 0m0.218s  0m0.208s  0m0.200s  sys  | 0m0.170s  0m0.250s  0m0.215s


Parallel Implementations of GZIP (cont)
//======================================================================
*         14 Threads                          
*/

pigz
//**********************************************************************
time pigz -p 14 <$input >pigz.gz         time pigz <$input >pigz.gz           
     | Trial 1   Trial 2   Trial 3        | Trial 1   Trial 2   Trial 3   
-----------------------------------  -----------------------------------  
real | 0m2.573s  0m2.564s  0m2.649s  real | 
user | 0m9.087s  0m9.076s  0m9.081s  user | 
sys  | 0m0.140s  0m0.170s  0m0.153s  sys  | 

Pigzj
//**********************************************************************
time java Pigzj -p 14 <$input >Pigzj.gz  time java Pigzj <$input >Pigzj.gz    
     | Trial 1   Trial 2   Trial 3        | Trial 1   Trial 2   Trial 3   
-----------------------------------  -----------------------------------  
real | 0m2.884s  0m2.861s  0m2.918s  real | 
user | 0m9.306s  0m9.375s  0m9.358s  user | 
sys  | 0m0.271s  0m0.280s  0m0.265s  sys  | 

pigzj
//**********************************************************************
time ./pigzj -p 14 <$input >pigzj.gz     time ./pigzj <$input >pigzj.gz       
     | Trial 1   Trial 2   Trial 3        | Trial 1   Trial 2   Trial 3   
-----------------------------------  -----------------------------------  
real | 0m2.742s  0m2.699s  0m2.747s  real | 
user | 0m9.226s  0m9.246s  0m9.220s  user | 
sys  | 0m0.203s  0m0.173s  0m0.205s  sys  | 


Summary
//======================================================================
//
//                              AVERAGES
//
//======================================================================



               AVERAGE TIME PER BINARY AND NUMBER THREADS
//======================================================================
TABLE A
            | default   8 threads 14 threads
-------------------------------------------
gzip   real | 0m9.595s  -          -
gzip   user | 0m9.120s  -          -
gzip   sys  | 0m0.087s  -          -
-------------------------------------------
pigz   real | 0m2.595s  0m2.975s  0m2.595s
pigz   user | 0m9.081s  0m9.209s  0m9.081s
pigz   sys  | 0m0.154s  0m0.199s  0m0.154s
-------------------------------------------
Pigzj  real | 0m2.907s  0m3.002s  0m2.596s
Pigzj  user | 0m9.294s  0m9.314s  0m9.081s
Pigzj  sys  | 0m0.285s  0m0.292s  0m0.154s
-------------------------------------------
pigzj  real | 0m2.883s  0m2.859s  0m2.729s
pigzj  user | 0m9.195s  0m9.210s  0m9.231s
pigzj  sys  | 0m0.209s  0m0.212s  0m0.194s


                AVERAGE TIME PER BINARY AND NUMBER THREADS
//======================================================================
TABLE B                 TABLE C
    PER     |   All 
   BINARY   | Threads 
----------------------  
gzip   real | 0m9.595s          ALL
gzip   user | 0m9.120s       PARALLEL         ALL 
gzip   sys  | 0m0.087s       BINARIES     | THREADS
----------------------  ----------------------------
pigz   real | 0m2.722s        gzip   real | 0m9.595s
pigz   user | 0m9.124s        gzip   user | 0m9.120s
pigz   sys  | 0m0.169s        gzip   sys  | 0m0.087s
----------------------  ----------------------------
Pigzj  real | 0m2.825s  [pP]igz[j]   real | 0m2.790s 
Pigzj  user | 0m9.230s  [pP]igz[j]   user | 0m9.190s 
Pigzj  sys  | 0m0.244s  [pP]igz[j]   sys  | 0m0.206s 
----------------------  ----------------------------
pigzj  real | 0m2.824s  
pigzj  user | 0m9.217s  
pigzj  sys  | 0m0.205s  


Compression ratios:
gzip    67.5%
pigz    67.6%
Pigzj   67.6%
pigzj   67.6%


Note:

Much of the design is based off Mess Admin's. I thought the overal
structure, while tough to look at blindly, was decently laid out.
In my implementation of Pigzj, I did not include all extended classes,
I altered dependencies between modules, changed configuration parameters,
essentially aiming to decouple the code a little further than it 
already was, though admittedly, this proved more a hassle than not.

For example, with the Block Module. I do not like the dependancy the module
currently has with Outstream. I think output should be limited to the write
module. While it is true that block has a read pipe dependancy, I would prefer
to yank this into readTask, though for how Block is configured, and for the
sake of modularity, I choose to keep the layout.

One current limitation the module currently has is it is unable to produce an 
error (as expected) for the following command:

bash-4.4$ java Pigzj </dev/zero >/dev/full

This implementation hangs. I suspect I might need to switch to a piped stream
or something that will throw an error.

The following commands all execute without error:

input=/usr/local/cs/graalvm-ce-java17-22.3.1/lib/modules
bash-4.4$ time gzip <$input >gzip.gz
bash-4.4$ time pigz <$input >pigz.gz
bash-4.4$ time java Pigzj <$input >Pigzj.gz
bash-4.4$ time ./pigzj <$input >pigzj.gz
bash-4.4$ ls -l gzip.gz pigz.gz Pigzj.gz pigzj.gz

# This checks Pigzj's and pigzj's output.
bash-4.4$ gzip -d <Pigzj.gz | cmp - $input
bash-4.4$ gzip -d <pigzj.gz | cmp - $input


Considering average table C above, we immediately see that single-threaded 
compression (gzip) differs from multi-threaded compression (pigz, Pigzj, 
pigzj) by a near factor of 3 in real (clock) time, i.e. the time a user 
experience waiting for the successful completion of the program.

Another observation: during all compressions, the time the cpu speant
processesing function calls (user time) is roughly the same between single-
and multi-threaded implementations of gzip compression. This is an expected
behavior: All the work that needs to happen needs to happen. So regardless
the number of threads used, a file of some fixed size, being read through
standard input, compressed, and written to standard output, will always
require the same amount of processing time to compute, since it is the 
same amout of work. After all, compression a computation without shortcuts.

Another noticable and notable differnce between single-/multi- threaded
implementations is the amount of time the program spends in the kernal
(sys time). Again, we would exepct this, especially in synchronized parallel 
programs. This is mostly due to trapping when threads wait for locks, when
they release locks, etc. Synchronization generally involves a syscall to 
notify waiting threads/wake threads up.


Consideration As Thread Counts Increase

Syscalls are generally expensive operations, and so finding the right ratio
between the number of threads-to-processors and taking care not to bottle neck
single-threaded operations, like IO is imporant.

This balance is critical. If the thread-to-processor ratio is too high (i.e.,
more than 4 threads per processor, say), then we run into software scheduling
issues. The Software scheduler runs heuristics to deterime the optimal number
of threads to use per core. If a user defines more threads than the scheduler
can efficiently schedule, the scheduler may do one of the following to use
the threads a user defined:

A) Diminish thread time-slices as the number of threads per core increases.
This has the effect of "cramming" more threads on a single core (for the same
OS provided timeslice). Here are the side effects:
    1) Less dedicated per-thread core time and 
    2) Increase in context-switching time between threads
    
B) Keep the thread-to-core ratio the same and spread threads out over
multiple process time-slices. This has the same effect. While each thread
may still have the same access to the core, synchronized accesses take longer
and threads spend more time waiting than doing anything.

Consideration of Stack Traces

* gzip has nearly 4x more read calls than pigz.
* gzip has this highest read and write calls of all programs executed.
* All multi-threaded programs make incrementally more futex calls, and 
memory access calls than gzip.
* Pigzj has almost no read calls, relative to all other binaries (gzip included)
* the native-image pigzj of Pigzj has the most interesting stack trace. 
It has a large number of execve calls which across every trace which 
indicates graalvm might be trowing compress tasks into a process and 
letting the OS schedule tasks. 
* the native-image also has significantly less futex calls and waits,
which futher indicates that graalvm could throwing threads into processes
with a socket straight to the write channel. This could explain the excessive
openat, close, sockst, execve, mmap, calls.

Stack Traces in Relation to Times noted above.
The stack traces back up the above notions, in particular Pigzj. further,
multi-threaded implementations rely less on moving physical bytes around
and more on memory operations which helps keep the io bus open for
business.



//======================================================================
//
//                              STACK TRACES
//
//======================================================================

//=================================================================
//
//                            GZIP
//
//=================================================================

% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 85.41    0.005789          28       203           write
 14.30    0.000969           0      4973           read
  0.12    0.000008           0        10           rt_sigaction
  0.10    0.000007           2         3           fstat
  0.07    0.000005           5         1         1 ioctl
  0.00    0.000000           0         4           close
  0.00    0.000000           0         1           lseek
  0.00    0.000000           0         6           mmap
  0.00    0.000000           0         4           mprotect
  0.00    0.000000           0         1           munmap
  0.00    0.000000           0         1           brk
  0.00    0.000000           0         1         1 access
  0.00    0.000000           0         1           execve
  0.00    0.000000           0         2         1 arch_prctl
  0.00    0.000000           0         2           openat
------ ----------- ----------- --------- --------- ----------------
100.00    0.006778           1      5213         3 total


//=================================================================
//
//                            PIGZ
//
//=================================================================
Default
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 78.81    0.095948         113       843         6 futex
 20.20    0.024597          19      1253           read
  0.36    0.000433          19        22           munmap
  0.21    0.000255          51         5           clone
  0.14    0.000175          11        15           mprotect
  0.11    0.000135           4        28           mmap
  0.05    0.000055           9         6           openat
  0.02    0.000028           4         6           fstat
  0.02    0.000027           4         6           close
  0.02    0.000020           2         8           brk
  0.01    0.000014           4         3           lseek
  0.01    0.000012           4         3           rt_sigaction
  0.01    0.000010           5         2         2 ioctl
  0.01    0.000008           4         2         1 arch_prctl
  0.01    0.000007           7         1         1 access
  0.00    0.000005           5         1           set_robust_list
  0.00    0.000005           5         1           prlimit64
  0.00    0.000004           4         1           rt_sigprocmask
  0.00    0.000004           4         1           set_tid_address
  0.00    0.000000           0         1           execve
------ ----------- ----------- --------- --------- ----------------
100.00    0.121742          55      2208        10 total


pigs: 8 Threads
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 78.98    0.097811         145       672         2 futex
 20.38    0.025234          20      1253           read
  0.26    0.000325         325         1           execve
  0.19    0.000238           6        38           mmap
  0.04    0.000051           2        19           mprotect
  0.04    0.000051           5         9           clone
  0.03    0.000035           5         6           openat
  0.02    0.000024           4         6           fstat
  0.02    0.000023           3         6           close
  0.01    0.000012           4         3           lseek
  0.01    0.000012           0        40           munmap
  0.01    0.000008           4         2         1 arch_prctl
  0.01    0.000007           7         1         1 access
  0.00    0.000005           0        10           brk
  0.00    0.000000           0         3           rt_sigaction
  0.00    0.000000           0         1           rt_sigprocmask
  0.00    0.000000           0         1         1 ioctl
  0.00    0.000000           0         1           set_tid_address
  0.00    0.000000           0         1           set_robust_list
  0.00    0.000000           0         1           prlimit64
------ ----------- ----------- --------- --------- ----------------
100.00    0.123836          59      2074         5 total


pigz: 14 Threads
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 65.31    0.098875         190       518         9 futex
 32.78    0.049634          39      1253           read
  0.91    0.001372          19        69           munmap
  0.79    0.001193          79        15           clone
  0.14    0.000208           8        25           mprotect
  0.07    0.000106           1        55           mmap
  0.01    0.000013           1        12           brk
  0.00    0.000000           0         6           close
  0.00    0.000000           0         6           fstat
  0.00    0.000000           0         3           lseek
  0.00    0.000000           0         3           rt_sigaction
  0.00    0.000000           0         1           rt_sigprocmask
  0.00    0.000000           0         1         1 ioctl
  0.00    0.000000           0         1         1 access
  0.00    0.000000           0         1           execve
  0.00    0.000000           0         2         1 arch_prctl
  0.00    0.000000           0         1           set_tid_address
  0.00    0.000000           0         6           openat
  0.00    0.000000           0         1           set_robust_list
  0.00    0.000000           0         1           prlimit64
------ ----------- ----------- --------- --------- ----------------
100.00    0.151401          76      1980        12 total


//=================================================================
//
//                            PIGZJ
//
//=================================================================
Default:
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 99.40    0.224317      112158         2           futex
  0.21    0.000483           8        56        45 openat
  0.12    0.000270           6        39        36 stat
  0.08    0.000175           7        25           mmap
  0.05    0.000120           6        18           mprotect
  0.03    0.000069           5        13           read
  0.02    0.000049           4        11           close
  0.02    0.000048           4        11           fstat
  0.01    0.000030          15         2           readlink
  0.01    0.000018          18         1           clone
  0.01    0.000015           7         2           munmap
  0.01    0.000014           2         6           brk
  0.01    0.000014           7         2         1 access
  0.01    0.000012           4         3           lseek
  0.00    0.000010          10         1           execve
  0.00    0.000008           4         2         1 arch_prctl
  0.00    0.000006           3         2           rt_sigaction
  0.00    0.000003           3         1           rt_sigprocmask
  0.00    0.000003           3         1           set_tid_address
  0.00    0.000003           3         1           set_robust_list
  0.00    0.000003           3         1           prlimit64
  0.00    0.000000           0         1           getpid
------ ----------- ----------- --------- --------- ----------------
100.00    0.225670        1122       201        83 total


Pigzj: 8 Threads
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 99.38    0.270922      135461         2           futex
  0.17    0.000473           8        56        45 openat
  0.13    0.000342         342         1           execve
  0.08    0.000229           5        39        36 stat
  0.06    0.000170           6        25           mmap
  0.05    0.000144           8        18           mprotect
  0.02    0.000065           5        13           read
  0.02    0.000052           4        11           close
  0.02    0.000049           4        11           fstat
  0.01    0.000030          15         2           readlink
  0.01    0.000027          13         2           munmap
  0.01    0.000024           4         6           brk
  0.01    0.000017           8         2         1 access
  0.01    0.000015          15         1           clone
  0.00    0.000012           4         3           lseek
  0.00    0.000008           4         2           rt_sigaction
  0.00    0.000008           4         2         1 arch_prctl
  0.00    0.000005           5         1           rt_sigprocmask
  0.00    0.000004           4         1           set_tid_address
  0.00    0.000004           4         1           set_robust_list
  0.00    0.000004           4         1           prlimit64
  0.00    0.000003           3         1           getpid
------ ----------- ----------- --------- --------- ----------------
100.00    0.272607        1356       201        83 total


Pigsj: 14 Threads
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ----------------
 99.82    0.254434      127217         2           futex
  0.03    0.000089           1        56        45 openat
  0.03    0.000074           4        18           mprotect
  0.03    0.000069           2        25           mmap
  0.01    0.000035          17         2           munmap
  0.01    0.000030           0        39        36 stat
  0.01    0.000026           2        13           read
  0.01    0.000024           4         6           brk
  0.01    0.000023           2        11           close
  0.01    0.000023           2        11           fstat
  0.01    0.000023          23         1           clone
  0.00    0.000012           6         2           readlink
  0.00    0.000011           5         2         1 access
  0.00    0.000008           4         2           rt_sigaction
  0.00    0.000005           5         1           rt_sigprocmask
  0.00    0.000004           4         1           set_tid_address
  0.00    0.000004           4         1           set_robust_list
  0.00    0.000004           4         1           prlimit64
  0.00    0.000003           3         1           getpid
  0.00    0.000000           0         3           lseek
  0.00    0.000000           0         1           execve
  0.00    0.000000           0         2         1 arch_prctl
------ ----------- ----------- --------- --------- ----------------
100.00    0.254901        1268       201        83 total

//=================================================================
//
//                      (native-image) pigzj
//
//=================================================================
default
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ------------------
 67.80    0.087884          44      1986         5 futex
 29.27    0.037943          29      1290           read
  1.19    0.001540        1540         1           execve
  0.78    0.001011           5       186           brk
  0.16    0.000211          10        21           openat
  0.15    0.000199           9        20           mprotect
  0.14    0.000183           2        69           mmap
  0.12    0.000156          22         7           clone
  0.07    0.000096           4        23           close
  0.07    0.000094           2        32         7 lseek
  0.07    0.000086           2        31           fstat
  0.06    0.000084           4        17           munmap
  0.02    0.000030          10         3           socket
  0.02    0.000026          13         2           getdents64
  0.02    0.000020          20         1           write
  0.01    0.000018           3         6           rt_sigaction
  0.01    0.000011           2         5           sched_getaffinity
  0.01    0.000011           1         8           prlimit64
  0.00    0.000006           6         1         1 access
  0.00    0.000005           5         1         1 getsockname
  0.00    0.000004           4         1           setsockopt
  0.00    0.000004           2         2         1 arch_prctl
  0.00    0.000003           1         2           rt_sigprocmask
  0.00    0.000002           2         1           set_tid_address
  0.00    0.000001           1         1           set_robust_list
------ ----------- ----------- --------- --------- ------------------
100.00    0.129628          34      3717        15 total


(native-image) pigzj: 8 Threads
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ------------------
 59.39    0.086757          35      2458         3 futex
 39.58    0.057814          44      1292           read
  0.25    0.000361         361         1           execve
  0.17    0.000251           2        89           mmap
  0.12    0.000168           8        21           openat
  0.09    0.000128           5        24           mprotect
  0.07    0.000097           4        23           close
  0.06    0.000094           5        18           munmap
  0.06    0.000088           8        11           clone
  0.04    0.000061           1        31           fstat
  0.03    0.000046           0       126           brk
  0.03    0.000042           1        32         7 lseek
  0.02    0.000032           4         8           prlimit64
  0.02    0.000026           8         3           socket
  0.02    0.000026           5         5           sched_getaffinity
  0.02    0.000025           4         6           rt_sigaction
  0.02    0.000023          11         2           getdents64
  0.01    0.000009           4         2           rt_sigprocmask
  0.01    0.000009           4         2         1 arch_prctl
  0.01    0.000008           8         1         1 access
  0.00    0.000005           5         1         1 getsockname
  0.00    0.000005           5         1           setsockopt
  0.00    0.000004           4         1           set_tid_address
  0.00    0.000004           4         1           set_robust_list
  0.00    0.000000           0         1           write
------ ----------- ----------- --------- --------- ------------------
100.00    0.146083          35      4160        13 total


(native-image) pigzj: 14 Threads
% time     seconds  usecs/call     calls    errors syscall
------ ----------- ----------- --------- --------- ------------------
 52.83    0.081178          43      1872         4 futex
 44.85    0.068915          53      1294           read
  1.28    0.001970           2       898           brk
  0.17    0.000263          12        21           openat
  0.17    0.000260           2       119           mmap
  0.15    0.000226           7        30           mprotect
  0.12    0.000181          22         8           prlimit64
  0.09    0.000136           4        31           fstat
  0.09    0.000134           4        32         7 lseek
  0.08    0.000121           5        23           close
  0.07    0.000103           6        17           clone
  0.03    0.000041           2        18           munmap
  0.02    0.000026           8         3           socket
  0.02    0.000025           4         6           rt_sigaction
  0.02    0.000024          12         2           getdents64
  0.01    0.000021          21         1           write
  0.01    0.000015           3         5           sched_getaffinity
  0.01    0.000008           4         2           rt_sigprocmask
  0.00    0.000005           5         1         1 getsockname
  0.00    0.000005           5         1           setsockopt
  0.00    0.000005           2         2         1 arch_prctl
  0.00    0.000004           4         1           set_tid_address
  0.00    0.000004           4         1           set_robust_list
  0.00    0.000000           0         1         1 access
  0.00    0.000000           0         1           execve
------ ----------- ----------- --------- --------- ------------------
100.00    0.153670          35      4390        14 total
