package com.vztekoverflow.cilostazol.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class BenchmarkValidityTests extends TestBase {
  @Test
  public void nBody() {
    var result =
        runTestFromCode(
            """
                namespace nbody
                {
                    /*  The Computer Language Benchmarks Game
                        https://salsa.debian.org/benchmarksgame-team/benchmarksgame/

                        contributed by Isaac Gouy
                        modified by Robert F. Tobler
                        modified by Eric P. Nusbaum
                    */

                    using System;

                    public class NBody
                    {
                        public static void Main(String[] args)
                        {
                                int n = args.Length > 0 ? Int32.Parse(args[0]) : 1_000;
                                NBodySystem bodies = new NBodySystem();
                                Console.WriteLine(bodies.Energy());
                                for (int i = 0; i < n; i++) bodies.Advance(0.01);
                                Console.WriteLine(bodies.Energy());
                        }
                    }

                    public class Body { public double x, y, z, vx, vy, vz, mass; }

                    public class NBodySystem
                    {
                        private Body[] _bodies;
                        private Body[] _pairL;
                        private Body[] _pairR;
                        private byte bodyCount = 5;

                        const double Pi = 3.141592653589793;
                        const double Solarmass = 4 * Pi * Pi;
                        const double DaysPeryear = 365.24;

                        public NBodySystem()
                        {
                            _bodies = new[]
                            {
                                new Body()
                                {
                                    // Sun
                                    mass = Solarmass,
                                },
                                new Body()
                                {
                                    // Jupiter
                                    x = 4.84143144246472090e+00,
                                    y = -1.16032004402742839e+00,
                                    z = -1.03622044471123109e-01,
                                    vx = 1.66007664274403694e-03*DaysPeryear,
                                    vy = 7.69901118419740425e-03*DaysPeryear,
                                    vz = -6.90460016972063023e-05*DaysPeryear,
                                    mass = 9.54791938424326609e-04*Solarmass,
                                },
                                new Body()
                                {
                                    // Saturn
                                    x = 8.34336671824457987e+00,
                                    y = 4.12479856412430479e+00,
                                    z = -4.03523417114321381e-01,
                                    vx = -2.76742510726862411e-03*DaysPeryear,
                                    vy = 4.99852801234917238e-03*DaysPeryear,
                                    vz = 2.30417297573763929e-05*DaysPeryear,
                                    mass = 2.85885980666130812e-04*Solarmass,
                                },
                                new Body()
                                {
                                    // Uranus
                                    x = 1.28943695621391310e+01,
                                    y = -1.51111514016986312e+01,
                                    z = -2.23307578892655734e-01,
                                    vx = 2.96460137564761618e-03*DaysPeryear,
                                    vy = 2.37847173959480950e-03*DaysPeryear,
                                    vz = -2.96589568540237556e-05*DaysPeryear,
                                    mass = 4.36624404335156298e-05*Solarmass,
                                },
                                new Body()
                                {
                                    // Neptune
                                    x = 1.53796971148509165e+01,
                                    y = -2.59193146099879641e+01,
                                    z = 1.79258772950371181e-01,
                                    vx = 2.68067772490389322e-03*DaysPeryear,
                                    vy = 1.62824170038242295e-03*DaysPeryear,
                                    vz = -9.51592254519715870e-05*DaysPeryear,
                                    mass = 5.15138902046611451e-05*Solarmass,
                                },
                            };

                            _pairL = new Body[(bodyCount * (bodyCount - 1) / 2)];
                            _pairR = new Body[(bodyCount * (bodyCount - 1) / 2)];
                            var pi = 0;
                            for (var i = 0; i < bodyCount - 1; i++)
                                for (var j = i + 1; j < bodyCount; j++)
                                {
                                    _pairL[pi] = _bodies[i];
                                    _pairR[pi] = _bodies[j];
                                    pi++;
                                }

                        double px = 0.0, py = 0.0, pz = 0.0;
                            foreach (var b in _bodies)
                            {
                                px += b.vx * b.mass; py += b.vy * b.mass; pz += b.vz * b.mass;
                            }
                            var sol = _bodies[0];
                            sol.vx = -px / Solarmass; sol.vy = -py / Solarmass; sol.vz = -pz / Solarmass;
                        }

                        public void Advance(double dt)
                        {
                            var length = _pairL.Length;
                            for (int i = 0; i < length; i++)
                            {
                                Body bi =  _pairL[i], bj = _pairR[i];
                                double dx = bi.x - bj.x, dy = bi.y - bj.y, dz = bi.z - bj.z;
                                double d2 = dx * dx + dy * dy + dz * dz;
                                double mag = dt / (d2 * Math.Sqrt(d2));
                                bi.vx -= dx * bj.mass * mag; bj.vx += dx * bi.mass * mag;
                                bi.vy -= dy * bj.mass * mag; bj.vy += dy * bi.mass * mag;
                                bi.vz -= dz * bj.mass * mag; bj.vz += dz * bi.mass * mag;
                            }
                            foreach (var b in _bodies)
                            {
                                b.x += dt * b.vx; b.y += dt * b.vy; b.z += dt * b.vz;
                            }
                        }

                        public double Energy()
                        {
                            double e = 0.0;
                            for (int i = 0; i < bodyCount; i++)
                            {
                                var bi = _bodies[i];
                                e += 0.5 * bi.mass * (bi.vx * bi.vx + bi.vy * bi.vy + bi.vz * bi.vz);
                                for (int j = i + 1; j < bodyCount; j++)
                                {
                                    var bj = _bodies[j];
                                    double dx = bi.x - bj.x, dy = bi.y - bj.y, dz = bi.z - bj.z;
                                    e -= (bi.mass * bj.mass) / Math.Sqrt(dx * dx + dy * dy + dz * dz);
                                }
                            }
                            return e;
                        }
                    }
                }
                """);
    assertEquals(
        "-0.16907516382852447\n-0.169087605234606\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void simple() {
    var result =
        runTestFromCode(
            """
              /* The Computer Language Benchmarks Game
                 https://salsa.debian.org/benchmarksgame-team/benchmarksgame/

                 line-by-line from Greg Buchholz's C program
              */

              using System;

              class Simple
              {
                  public static void Main(String[] args)
                  {
                      int w, h, x, y, bit_num = 0;
                      int byte_acc = 0;
                      int i, iter = 50;
                      double limit = 2.0;
                      double Zr, Zi, Cr, Ci, Tr, Ti;
                      w = h = 20;

                      Console.WriteLine("P4");
                      Console.WriteLine(w + " " + h);

                      for (y = 0; y < h; y++)
                      {
                          for (x = 0; x < w; x++)
                          {
                              Zr = 0.0;
                              Zi = 0.0;
                              Cr = (2 * (double) x / w - 1.5);
                              Ci = (2 * (double) y / h - 1);

                              for (i = 0; i < iter; i++)
                              {
                                  Tr = Zr * Zr - Zi * Zi + Cr;
                                  Ti = 2 * Zr * Zi + Ci;
                                  Zr = Tr;
                                  Zi = Ti;
                                  if (Zr * Zr + Zi * Zi > limit * limit)
                                      break;
                              }

                              if (Zr * Zr + Zi * Zi > limit * limit)
                                  byte_acc = (byte_acc << 1) | 0x00;
                              else
                                  byte_acc = (byte_acc << 1) | 0x01;

                              bit_num++;

                              if (bit_num == 8)
                              {
                                  Console.Write((byte) byte_acc);
                                  byte_acc = 0;
                                  bit_num = 0;
                              }
                              else if (x == w - 1)
                              {
                                  byte_acc = byte_acc << (8 - w % 8);
                                  Console.Write((byte) byte_acc);
                                  byte_acc = 0;
                                  bit_num = 0;
                              }
                          }
                      }
                  }
              }
              """);
    assertEquals(
        """
                    P4
                    20 20
                    010000060060047128063224012722401272241425522431255224255255192312552241425522401272240127224063224047128060060000""",
        result.output().replace("\r\n", "\n"));
  }

  @Test
  public void binaryTrees() {
    var result =
        runTestFromCode(
            """
          // The Computer Language Benchmarks Game
          // https://benchmarksgame-team.pages.debian.net/benchmarksgame/
          //
          // based Jarkko Miettinen Java #2 and Anthony Lloyd C#\s
          // contributed by Isaac Gouy

          using System;

          class BinaryTrees
          {
              const int MinDepth = 4;
              const int NoTasks = 4;

              public static void Main(string[] args)
              {
                  int maxDepth = 10;

                  Console.WriteLine(string.Concat("stretch tree of depth ", maxDepth + 1,
                      "\\t check: ", (TreeNode.bottomUpTree(maxDepth + 1)).itemCheck()));

                  var longLivedTree = TreeNode.bottomUpTree(maxDepth);

                  var results = new string[(maxDepth - MinDepth) / 2 + 1];

                  for (int i = 0; i < results.Length; i++)
                  {
                      int depth = i * 2 + MinDepth;
                      int n = (1 << maxDepth - depth + MinDepth) / NoTasks;
                      var check = 0;
                      for (int t = 0; t < NoTasks; t++)
                      {
                          for (int j = n; j > 0; j--)
                              check += (TreeNode.bottomUpTree(depth)).itemCheck();
                      }

                      results[i] = string.Concat(n * NoTasks, "\\t trees of depth ",
                          depth, "\\t check: ", check);
                  }

                  for (int i = 0; i < results.Length; i++)
                      Console.WriteLine(results[i]);

                  Console.WriteLine(string.Concat("long lived tree of depth ", maxDepth,
                      "\\t check: ", longLivedTree.itemCheck()));
              }

              private class TreeNode
              {
                  readonly TreeNode left, right;

                  internal static TreeNode bottomUpTree(int depth)
                  {
                      if (depth > 0)
                      {
                          return new TreeNode(
                              bottomUpTree(depth - 1),
                              bottomUpTree(depth - 1));
                      }
                      else
                      {
                          return new TreeNode(null, null);
                      }
                  }

                  internal TreeNode(TreeNode left, TreeNode right)
                  {
                      this.left = left;
                      this.right = right;
                  }

                  internal int itemCheck()
                  {
                      if (left == null) return 1;
                      else return 1 + left.itemCheck() + right.itemCheck();
                  }
              }
          }
          """);
    assertEquals(
        """
                    stretch tree of depth 11\t check: 4095
                    1024\t trees of depth 4\t check: 31744
                    256\t trees of depth 6\t check: 32512
                    64\t trees of depth 8\t check: 32704
                    16\t trees of depth 10\t check: 32752
                    long lived tree of depth 10\t check: 2047
                    """,
        result.output().replace("\r\n", "\n"));
  }

  @Test
  public void fannkuchRedux() {
    var result =
        runTestFromCode(
            """
          /* The Computer Language Benchmarks Game
             https://salsa.debian.org/benchmarksgame-team/benchmarksgame/

             contributed by Isaac Gouy, transliterated from Oleg Mazurov's Java program
             concurrency fix and minor improvements by Peperud
             parallel and small optimisations by Anthony Lloyd
          */

          using System;

          public static class FannkuchRedux
          {
              static int taskCount;
              static int[] fact;

              static void firstPermutation(int[] p, int[] pp, int[] count, int idx)
              {
                  for (int i = 0; i < p.Length; ++i) p[i] = i;
                  for (int i = count.Length - 1; i > 0; --i)
                  {
                      int d = idx / fact[i];
                      count[i] = d;
                      if (d > 0)
                      {
                          idx = idx % fact[i];
                          for (int j = i; j >= 0; --j) pp[j] = p[j];
                          for (int j = 0; j <= i; ++j) p[j] = pp[(j + d) % (i + 1)];
                      }
                  }
              }

              static int nextPermutation(int[] p, int[] count)
              {
                  int first = p[1];
                  p[1] = p[0];
                  p[0] = first;
                  int i = 1;
                  while (++count[i] > i)
                  {
                      count[i++] = 0;
                      int next = p[1];
                      p[0] = next;
                      for (int j = 1; j < i;) p[j] = p[++j];
                      p[i] = first;
                      first = next;
                  }

                  return first;
              }

              static int countFlips(int first, int[] p, int[] pp)
              {
                  if (first == 0) return 0;
                  if (p[first] == 0) return 1;
                  for (int i = 0; i < pp.Length; i++) pp[i] = p[i];
                  int flips = 2;
                  while (true)
                  {
                      for (int lo = 1, hi = first - 1; lo < hi; lo++, hi--)
                      {
                          int t = pp[lo];
                          pp[lo] = pp[hi];
                          pp[hi] = t;
                      }

                      int tp = pp[first];
                      if (pp[tp] == 0) return flips;
                      pp[first] = first;
                      first = tp;
                      flips++;
                  }
              }

              public static void Main(string[] args)
              {
                  int n = args.Length > 0 ? int.Parse(args[0]) : 7;
                  fact = new int[n + 1];
                  fact[0] = 1;
                  var factn = 1;
                  for (int i = 1; i < fact.Length; i++)
                  {
                      fact[i] = factn *= i;
                  }

                  taskCount = n > 9 ? factn / (7 * 6 * 5 * 4 * 3 * 2) : 8;
                  int taskSize = factn / taskCount;
                  int nThreads = 8;
                  int chksum = 0, maxflips = 0;
                  for (int i = 0; i < nThreads; i++)
                  {
                      int[] p = new int[n], pp = new int[n], count = new int[n];
                      int taskId = 0;
                      int currMaxFlips = 0;
                      while ((taskId = --taskCount) >= 0)
                      {
                          firstPermutation(p, pp, count, taskId * taskSize);
                          var flips = countFlips(p[0], p, pp);
                          chksum += flips;
                          if (flips > currMaxFlips) currMaxFlips = flips;
                          for (int j = 1; j < taskSize; j++)
                          {
                              flips = countFlips(nextPermutation(p, count), p, pp);
                              chksum += (1 - (j % 2) * 2) * flips;
                              if (flips > currMaxFlips) currMaxFlips = flips;
                          }
                      }

                      if (currMaxFlips > maxflips) maxflips = currMaxFlips;
                  }

                  Console.WriteLine(chksum + "\\nPfannkuchen(" + n + ") = " + maxflips);
              }
          }
          """);

    assertEquals("228\nPfannkuchen(7) = 16\n", result.output().replace("\r\n", "\n"));
  }

  @Test
  public void mandelbrot() {
    var result =
        runTestFromCode(
            """
          /* The Computer Language Benchmarks Game
             https://salsa.debian.org/benchmarksgame-team/benchmarksgame/
             \s
             started with Java #2 program (Krause/Whipkey/Bennet/AhnTran/Enotus/Stalcup)
             adapted for C# by Jan de Vaan
          */

          using System;

          public class MandelBrot
          {
              private static int n = 200;
              private static int[][] data;
              private static int lineCount = -1;

              private static double[] Crb;
              private static double[] Cib;

              static int getByte(int x, int y)
              {
                  int res = 0;
                  for (int i = 0; i < 8; i += 2)
                  {
                      double Zr1 = Crb[x + i];
                      double Zi1 = Cib[y];

                      double Zr2 = Crb[x + i + 1];
                      double Zi2 = Cib[y];

                      int b = 0;
                      int j = 49;
                      do
                      {
                          double nZr1 = Zr1 * Zr1 - Zi1 * Zi1 + Crb[x + i];
                          double nZi1 = Zr1 * Zi1 + Zr1 * Zi1 + Cib[y];
                          Zr1 = nZr1;
                          Zi1 = nZi1;

                          double nZr2 = Zr2 * Zr2 - Zi2 * Zi2 + Crb[x + i + 1];
                          double nZi2 = Zr2 * Zi2 + Zr2 * Zi2 + Cib[y];
                          Zr2 = nZr2;
                          Zi2 = nZi2;

                          if (Zr1 * Zr1 + Zi1 * Zi1 > 4)
                          {
                              b |= 2;
                              if (b == 3) break;
                          }

                          if (Zr2 * Zr2 + Zi2 * Zi2 > 4)
                          {
                              b |= 1;
                              if (b == 3) break;
                          }
                      } while (--j > 0);

                      res = (res << 2) + b;
                  }

                  return res ^ -1;
              }

              public static void Main(String[] args)
              {
                  if (args.Length > 0) n = Int32.Parse(args[0]);

                  int lineLen = (n - 1) / 8 + 1;
                  data = new int[n][];

                  Crb = new double[n + 7];
                  Cib = new double[n + 7];

                  double invN = 2.0 / n;
                  for (int i = 0; i < n; i++)
                  {
                      Cib[i] = i * invN - 1.0;
                      Crb[i] = i * invN - 1.5;
                  }

                  for (int i = 0; i < 8; i++)
                  {
                      int y;
                      while ((y = ++lineCount) < n)
                      {
                          var buffer = new int[lineLen];
                          for (int x = 0; x < lineLen; x++)
                          {
                              buffer[x] = (byte) getByte(x * 8, y);
                          }

                          data[y] = buffer;
                      }
                  }

                  Console.WriteLine("P4\\n" +  n + " " + n);
                  for (int y = 0; y < n; y++)
                  for (int i = 0; i < lineLen; i++)
                      Console.Write(data[y][i]);
              }
          }
          """);

    assertEquals(
        """
        P4
        200 200
        000000000000000000200000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000024000000000000000000000000012200000000000000000000000112400000000000000000000000012400000000000000000000000012400000000000000000000000112480000000000000000000000015255128000000000000000000000112725512800000000000000000000002552552080000000000000000000000255255252000000000000000000000012725524800000000000000000000012552552400000000000000000000000255255248000000000000000000000525525524000000000000000000000032552552480000000000000000000000255255252000000000000000000000225525524000000000000000000000032552552280000000000000000000000255255252000000000000000000000012725522400000000000000000000006325522400000000000000000000003125519200000000000000000064064031255128000000000000000000000192725412432000000000000000002651432552552451280000000000000000000104207255255255176800000000000000002112225525525525525012000000000000000016612552552552552552541240000000000000002198025525525525525525512006400000000000002255131912552552552552552400000000000000000127725525525525525525525200000000000000002551512552552552552552552531289864000000000000425519125525525525525525525512925112800000000000072551272552552552552552552551612550000000000000325525525525525525525525525522725400000000000001255255255255255255255255255247254000000000000001272552552552552552552552552512540000000000000015255255255255255255255255255255000000000000006325525525525525525525525525525512800000000000012552552552552552552552552552552520000000000000122552552552552552552552552552552500000000000000152552552552552552552552552552551920000000000001287255255255255255255255255255255240000000000000172032552552552552552552552552552552320000000000003122325525525525525525525525525525524600000000000039255255255255255255255255255255255252000000000000352552552552552552552552552552552552530000000000001125525525525525525525525525525525525319200000000000325525525525525525525525525525525525512800000000000125525525525525525525525525525525525412800000000000125525525525525525525525525525525525500000000000069255255255255255255255255255255255255132000000000007255255255255255255255255255255255255208000000000001672552552552552552552552552552552552552200000000000125525525525525525525525525525525525525525400000000000127255255255255255255255255255255255255255128000000000012725525525525525525525525525525525525525400000000000312552552552552552552552552552552552552520000000000195255255255255255255255255255255255255232000000016000632552552552552552552552552552552552552400000032032960151912552552552552552552552552552552552552240000000120003255255255255255255255255255255255255255240000001220240001255255255255255255255255255255255255255252000006324115500025525525525525525525525525525525525525524800000126472551120025525525525525525525525525525525525525524800001127127255240072552552552552552552552552552552552552552480000063255255249032552552552552552552552552552552552552552540000063255255255032552552552552552552552552552552552552552540000047255255255144152552552552552552552552552552552552552552551920000632552552552243255255255255255255255255255255255255255240000011272552552552407255255255255255255255255255255255255255248000002552552552552481525525525525525525525525525525525525525525200005325525525525525415255255255255255255255255255255255255255240000015255255255255252725525525525525525525525525525525525525524800003125525525525525514325525525525525525525525525525525525525524800007255255255255255152552552552552552552552552552552552552552540000152552552552552551432552552552552552552552552552552552552552320000632552552552552551432552552552552552552552552552552552552552400000632552552552552552072552552552552552552552552552552552552552400000312552552552552552072552552552552552552552552552552552552552400000632552552552552552072552552552552552552552552552552552552551760000632552552552552552392552552552552552552552552552552552552551280011441272552552552552552392552552552552552552552552552552552552551920012541272552552552552552552552552552552552552552552552552552552550001254632552552552552552552552552552552552552552552552552552552541280032552552552552552552552552552552552552552552552552552552552552540003255255255255255255255255255255255255255255255255255255255255252000725525525525525525525525525525525525525525525525525525525525524000031255255255255255255255255255255255255255255255255255255255255192002552552552552552552552552552552552552552552552552552552552552552550000312552552552552552552552552552552552552552552552552552552552551920007255255255255255255255255255255255255255255255255255255255255240000325525525525525525525525525525525525525525525525525525525525525200032552552552552552552552552552552552552552552552552552552552552540001254632552552552552552552552552552552552552552552552552552552541280012541272552552552552552552552552552552552552552552552552552552550001144127255255255255255239255255255255255255255255255255255255255192000063255255255255255239255255255255255255255255255255255255255128000063255255255255255207255255255255255255255255255255255255255176000031255255255255255207255255255255255255255255255255255255255240000063255255255255255207255255255255255255255255255255255255255240000063255255255255255143255255255255255255255255255255255255255240000015255255255255255143255255255255255255255255255255255255255232000072552552552552551525525525525525525525525525525525525525525400003125525525525525514325525525525525525525525525525525525525524800001525525525525525272552552552552552552552552552552552552552480000532552552552552541525525525525525525525525525525525525525524000000255255255255248152552552552552552552552552552552552552552520000112725525525524072552552552552552552552552552552552552552480000063255255255224325525525525525525525525525525525525525524000000472552552551441525525525525525525525525525525525525525525519200006325525525503255255255255255255255255255255255255255254000006325525524903255255255255255255255255255255255255255254000011271272552400725525525525525525525525525525525525525524800000126472551120025525525525525525525525525525525525525524800000632411550002552552552552552552552552552552552552552480000012202400012552552552552552552552552552552552552552520000000120003255255255255255255255255255255255255255240000003203296015191255255255255255255255255255255255255224000000016000632552552552552552552552552552552552552400000000000195255255255255255255255255255255255255232000000000003125525525525525525525525525525525525525200000000000127255255255255255255255255255255255255254000000000001272552552552552552552552552552552552552551280000000001255255255255255255255255255255255255255254000000000001672552552552552552552552552552552552552200000000000072552552552552552552552552552552552552080000000000069255255255255255255255255255255255255132000000000001255255255255255255255255255255255255000000000000125525525525525525525525525525525525412800000000000325525525525525525525525525525525525512800000000000112552552552552552552552552552552552531920000000000035255255255255255255255255255255255253000000000000392552552552552552552552552552552552520000000000003122325525525525525525525525525525524600000000000017203255255255255255255255255255255232000000000000128725525525525525525525525525525524000000000000001525525525525525525525525525525519200000000000001225525525525525525525525525525525000000000000001255255255255255255255255255255252000000000000006325525525525525525525525525525512800000000000001525525525525525525525525525525500000000000000127255255255255255255255255251254000000000000012552552552552552552552552552472540000000000000325525525525525525525525525522725400000000000007255127255255255255255255255161255000000000000042551912552552552552552552551292511280000000000000255151255255255255255255253128986400000000000001277255255255255255255252000000000000000225513191255255255255255240000000000000000219802552552552552552551200640000000000000166125525525525525525412400000000000000002112225525525525525012000000000000000000010420725525525517680000000000000000026514325525524512800000000000000000000192725412432000000000000000064064031255128000000000000000000000031255192000000000000000000000063255224000000000000000000000012725522400000000000000000000002552552520000000000000000000003255255228000000000000000000000225525524000000000000000000000002552552520000000000000000000003255255248000000000000000000000525525524000000000000000000000002552552480000000000000000000001255255240000000000000000000000012725524800000000000000000000002552552520000000000000000000000255255208000000000000000000000112725512800000000000000000000001525512800000000000000000000001124800000000000000000000000012400000000000000000000000012400000000000000000000000112400000000000000000000000012200000000000000000000000024000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000""",
        result.output().replace("\r\n", "\n"));
  }
}
