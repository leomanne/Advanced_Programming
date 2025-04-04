-- The defined functions are:

-- readMSet reads a text file and builds an MSet of strings where each element is the "ciao" of a word.

-- writeMSet writes to a file, one per line each element of the MSet with its multiplicity in the format elem - multiplicity.

-- main: loads specific files from a directory, performs some tests and writes the results to files.

module Main where

import MultiSet
import Data.Char (toLower)
import Data.List (sort)
import System.IO

-- ciao
-- Given a String, returns its ciao

ciao :: String -> String
ciao s = sort (map toLower s)

--  readMSet
-- Reads a text file the argument and returns,
-- in IO, an MSet containing the ciao of all words in the file, each with
-- its corresponding multiplicity.

readMSet :: FilePath -> IO (MSet String)
readMSet filename = do
  content <- readFile filename
  -- words splits the text into words using whitespace as delimiters
  let ws = words content
      -- Apply the function ciao to each word
      ciaoWords = map ciao ws
      -- Build the MSet starting from the empty multiset, adding each word
      mset = foldl add empty ciaoWords
  return mset

-- writeMSet
-- Given an MSet and a filename, writes to the file 
-- one line for each pair (element, multiplicity) in (a,b) format.
writeMSet :: MSet String -> FilePath -> IO ()
writeMSet mset filename = do
  let linesList = case mset of
                    MS pairs -> map (\(w, n) -> w ++ " - " ++ show n) pairs
  writeFile filename (unlines linesList)
  -- 'unlines' joins the lines inserting a newline character between each line.

-- main
--  First we loads files from the directory aux_files into multisets m1, m2, m3, and m4.
--
-- then we checks and prints:
-- m1 and m4 are not equal as multisets, but they contain the same elements (ignoring multiplicities).
-- m1 is equal to the union of multisets m2 and m3.
-- Writes m1 and m4 to the files "anag-out.txt" and "gana-out.txt" respectively.
main :: IO ()
main = do
  -- Load files from the directory "aux_files"
  m1 <- readMSet "anagram.txt"
  m2 <- readMSet "anagram-s1.txt"
  m3 <- readMSet "anagram-s2.txt"
  m4 <- readMSet "margana2.txt"
  
  --m1 and m4 should be different as multisets
  if m1 == m4
     then putStrLn "Error: m1 and m4 are equal as multisets, but they should be different."
     else putStrLn "OK: m1 and m4 are different as multisets."
  
  -- they should contain the same elements (ignoring multiplicities).
  -- We use our Foldable instance to obtain the list of distinct elements.
  let distinctM1 = sort (foldr (:) [] m1)
      distinctM4 = sort (foldr (:) [] m4)
  if distinctM1 == distinctM4
     then putStrLn "OK: m1 and m4 have the same elements (ignoring multiplicities)."
     else putStrLn "Error: m1 and m4 do not have the same elements (ignoring multiplicities)."
  
  -- m1 should be equal to the union of multisets m2 and m3.
  if m1 == union m2 m3
     then putStrLn "OK: m1 is equal to the union of m2 and m3."
     else putStrLn "Error: m1 is not equal to the union of m2 and m3."
  
  -- Write the results to files
  writeMSet m1 "anag-out.txt"
  writeMSet m4 "gana-out.txt"
  putStrLn "Files anag-out.txt and gana-out.txt have been written successfully."
