============================================
Programming Manual with Recursive Vob Scenes
============================================


Q: Can I ask coordinate system parameters from child scene?

   A: Phew, after 5 hours of debugging I would suggest
      that don't do that. Don not for example call getSqSize().
      It is absolutely working as it should but the child may 
      have not been placed into parent vob scene yet.

