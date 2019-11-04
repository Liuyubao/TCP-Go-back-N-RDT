## Revisions
10/12 - Fixed typo, please `checkout` to the latest version.

## Quick Start
- Clone the repo by `git clone https://github.com/jiup/rdt_skeleton.git`.
- Run `make` to compile the code.
- Execute `./rdt_sim 1000 0.1 100 0 0 0 0`.

The RDT layer is implemented in `MyRdtSender.java` and `MyRdtReceiver`, The current implementation assumes there is no packet loss, corruption, or reordering in the underlying link medium. You will need to enhance the implementation to deal with all these situations. In general, you are not supposed to change sourcecode under the folder `sim`.

For debugging purpose, you may want to add more print statements in the simulator. If you do so, definitely remember to test your program with the original simulator before turn-in.


## Testing
The provided simulation files should compile and run. However, they only work correctly when there is no packet loss, corruption, or reordering in the underlying link medium. Run `rdt_sim 1000 0.1 100 0 0 0 0` to see what happens. In summary, the following are a few test cases you may want to use.

- `rdt_sim 1000 0.1 100 0 0 0 0` - there is no packet loss, corruption, or reordering in the underlying link medium.
- `rdt_sim 1000 0.1 100 0.02 0 0 0` - there is no packet loss or corruption, but there is reordering in the underlying link medium.
- `rdt_sim 1000 0.1 100 0 0.02 0 0` - there is no packet corruption or reordering, but there is packet loss in the underlying link medium.
- `rdt_sim 1000 0.1 100 0 0 0.02 0` - there is no packet loss or reordering, but there is packet corruption in the underlying link medium.
- `rdt_sim 1000 0.1 100 0.02 0.02 0.02 0` - there could be packet loss, corruption, or reordering in the underlying link medium.

Of course, your goal is to make the last test case work. Keep in mind that even if your program works, it may still occasionally report errors due to the limitation of checksumming. Your program, however, should never report errors when there is no packet corruption in the underlying link medium. If your program works for some cases with parameters differing from the above examples, please specify these cases in your writeup to get partial credits.


## Submission
Please hand in your `MyRdtSender.java`, `MyRdtReceiver.java` and `writeup.pdf` for grading. When you are ready to submit, you can run `make submission` to help you pack everything needed.

You should NOT turn in the simulator code because we will use the original versions of those files in grading. If the default makefile doesn't work for you, you should also turn in a new makefile.
# TCP-Go-back-N-RDT
