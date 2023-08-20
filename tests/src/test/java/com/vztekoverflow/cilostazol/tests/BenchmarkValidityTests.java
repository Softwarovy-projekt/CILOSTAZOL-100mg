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
}
